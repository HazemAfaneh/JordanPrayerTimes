package com.mbf.wearable.jordanprayertimes.di

import com.mbf.wearable.jordanprayertimes.presentation.MainViewModel
import com.mbf.wearable.jordanprayertimes.presentation.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        MainViewModel(
            loadInitialHomeScreenDataUseCase = get()
        )
    }

    viewModel {
        SettingsViewModel(
            loadCitiesRepo = get()
        )
    }
}
