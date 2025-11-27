package com.mbf.wearable.jordanprayertimes.di

import com.mbf.wearable.jordanprayertimes.usecase.LoadInitialHomeScreenDataUseCase
import com.mbf.wearable.jordanprayertimes.usecase.impl.LoadInitialHomeScreenDataUseCaseImp
import org.koin.dsl.module

/**
 * Use case module for dependency injection
 * LoadInitialHomeScreenDataUseCase only loads prayer data
 * Cities are loaded separately in SettingsViewModel
 */
val useCaseModule = module {
    single<LoadInitialHomeScreenDataUseCase> {
        LoadInitialHomeScreenDataUseCaseImp(
            loadPrayer = get()
        )
    }
}
