package com.savestatus.wsstatussaver.update

import android.content.Context
import com.google.gson.GsonBuilder
import com.savestatus.wsstatussaver.BuildConfig
import com.savestatus.wsstatussaver.extensions.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private fun logInterceptor(): Interceptor {
    val loggingInterceptor = HttpLoggingInterceptor()
    if (BuildConfig.DEBUG) {
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
    } else {
        // disable retrofit log on release
        loggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
    }
    return loggingInterceptor
}

private fun headerInterceptor(context: Context): Interceptor {
    return Interceptor {
        val original = it.request()
        val request = original.newBuilder()
            .header("User-Agent", context.packageName)
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .method(original.method, original.body)
            .build()
        it.proceed(request)
    }
}

fun provideOkHttp(context: Context): OkHttpClient {
    return OkHttpClient.Builder()
        .addNetworkInterceptor(logInterceptor())
        .addInterceptor(headerInterceptor(context))
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
}

fun provideUpdateService(client: OkHttpClient): UpdateService {
    val gson = GsonBuilder()
        .setLenient()
        .create()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/repos/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .callFactory { request -> client.newCall(request) }
        .build()
    return retrofit.create(UpdateService::class.java)
}

fun Context.isAbleToUpdate(): Boolean {
    val minElapsedMillis = when (preferences().getUpdateSearchMode()) {
        UpdateSearchMode.EVERY_DAY -> TimeUnit.DAYS.toMillis(1)
        UpdateSearchMode.EVERY_FIFTEEN_DAYS -> TimeUnit.DAYS.toMillis(15)
        UpdateSearchMode.WEEKLY -> TimeUnit.DAYS.toMillis(7)
        UpdateSearchMode.MONTHLY -> TimeUnit.DAYS.toMillis(30)
        else -> -1
    }
    val elapsedMillis = System.currentTimeMillis() - preferences().lastUpdateSearch
    if ((minElapsedMillis >= -1) && elapsedMillis >= minElapsedMillis) {
        return isOnline(preferences().isUpdateOnlyWifi())
    }
    return false
}

const val DEFAULT_USER = "mardous"
const val DEFAULT_REPO = "WhatSave"