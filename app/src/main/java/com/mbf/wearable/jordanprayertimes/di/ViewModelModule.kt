package com.mbf.wearable.jordanprayertimes.di

import com.mbf.wearable.jordanprayertimes.presentation.MainViewModel
import com.mbf.wearable.jordanprayertimes.presentation.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * ViewModel module for dependency injection
 * Uses new Koin DSL (org.koin.core.module.dsl.viewModel)
 */
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
