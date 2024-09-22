package com.savestatus.wsstatussaver

import androidx.room.Room
import com.savestatus.wsstatussaver.database.MIGRATION_1_2
import com.savestatus.wsstatussaver.database.StatusDatabase
import com.savestatus.wsstatussaver.repository.*
import com.savestatus.wsstatussaver.storage.Storage
import com.savestatus.wsstatussaver.update.provideOkHttp
import com.savestatus.wsstatussaver.update.provideUpdateService
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

private val networkModule = module {
    factory {
        provideOkHttp(get())
    }
    single {
        provideUpdateService(get())
    }
}

private val dataModule = module {
    single {
        Room.databaseBuilder(androidContext(), StatusDatabase::class.java, "statuses.db")
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    factory {
        get<StatusDatabase>().statusDao()
    }

    factory {
        get<StatusDatabase>().messageDao()
    }
}

private val managerModule = module {
    single {
        PhoneNumberUtil.createInstance(androidContext())
    }
    single {
        Storage(androidContext())
    }
}

private val statusesModule = module {
    single {
        CountryRepositoryImpl(androidContext())
    } bind CountryRepository::class

    single {
        StatusesRepositoryImpl(androidContext(), get(), get())
    } bind StatusesRepository::class

    single {
        MessageRepositoryImpl(get())
    } bind MessageRepository::class

    single {
        RepositoryImpl(get(), get(), get())
    } bind Repository::class
}

private val viewModelModule = module {
    viewModel {
        WhatSaveViewModel(get(), get(), get())
    }
}

val appModules = listOf(networkModule, dataModule, managerModule, statusesModule, viewModelModule)