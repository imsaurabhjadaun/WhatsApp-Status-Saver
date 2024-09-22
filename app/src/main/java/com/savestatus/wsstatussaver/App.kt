package com.savestatus.wsstatussaver

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.savestatus.wsstatussaver.extensions.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun getApp(): App = App.instance

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Disable Analytics/Crashlytics for debug builds
        setAnalyticsEnabled(preferences().isAnalyticsEnabled())

        startKoin {
            androidContext(this@App)
            modules(appModules)
        }

        AppCompatDelegate.setDefaultNightMode(preferences().getDefaultDayNightMode())
    }

    val versionName: String
        get() = packageManager.packageInfo().versionName ?: "0"

    val versionCode: Int
        get() = packageManager.packageInfo().versionCode()

    companion object {
        internal lateinit var instance: App
            private set

        fun getFileProviderAuthority(): String = instance.packageName + ".file_provider"
    }
}