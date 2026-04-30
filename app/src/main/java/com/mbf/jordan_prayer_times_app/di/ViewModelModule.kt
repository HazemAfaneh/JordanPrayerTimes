package com.mbf.jordan_prayer_times_app.di

import com.mbf.jordan_prayer_times_app.presentation.MainViewModel
import com.mbf.jordan_prayer_times_app.presentation.SettingsViewModel
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
