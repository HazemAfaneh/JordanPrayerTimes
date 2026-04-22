package com.mbf.wearable.jordanprayertimes.di

import com.mbf.wearable.jordanprayertimes.presentation.MainViewModel
import com.mbf.wearable.jordanprayertimes.presentation.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainViewModel(
            loadPrayerTimesForCityUseCase = get(),
            cityPreferences = get()
        )
    }

    viewModel {
        SettingsViewModel(
            loadCitiesRepo = get(),
            cityPreferences = get()
        )
    }
}
