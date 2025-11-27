package com.mbf.wearable.jordanprayertimes.di

import com.mbf.wearable.jordanprayertimes.usecase.LoadInitialHomeScreenDataUseCase
import com.mbf.wearable.jordanprayertimes.usecase.impl.LoadInitialHomeScreenDataUseCaseImp
import org.koin.dsl.module

val useCaseModule = module {
    single<LoadInitialHomeScreenDataUseCase> {
        LoadInitialHomeScreenDataUseCaseImp(
            loadCitiesRepo = get(),
            loadPrayer = get()
        )
    }
}
