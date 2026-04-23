package com.mbf.wearable.jordanprayertimes.di

import com.mbf.wearable.jordanprayertimes.presentation.MainViewModel
import com.mbf.wearable.jordanprayertimes.presentation.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainViewModel(
            loadPrayerTimesForCityUseCase = get(),
            cityPreferences = get(),
            prayerAlarmScheduler = get(),
            application = androidApplication()
        )
    }

    viewModel {
        SettingsViewModel(
            loadCitiesRepo = get(),
            cityPreferences = get(),
            prayerAlarmScheduler = get()
        )
    }
}
