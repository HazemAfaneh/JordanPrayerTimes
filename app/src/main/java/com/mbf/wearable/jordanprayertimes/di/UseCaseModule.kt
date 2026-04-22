package com.mbf.wearable.jordanprayertimes.di

import com.mbf.wearable.jordanprayertimes.usecase.LoadInitialHomeScreenDataUseCase
import com.mbf.wearable.jordanprayertimes.usecase.LoadPrayerTimesForCityUseCase
import com.mbf.wearable.jordanprayertimes.usecase.impl.LoadInitialHomeScreenDataUseCaseImp
import com.mbf.wearable.jordanprayertimes.usecase.impl.LoadPrayerTimesForCityUseCaseImp
import org.koin.dsl.module

val useCaseModule = module {
    single<LoadInitialHomeScreenDataUseCase> {
        LoadInitialHomeScreenDataUseCaseImp(loadPrayer = get())
    }

    single<LoadPrayerTimesForCityUseCase> {
        LoadPrayerTimesForCityUseCaseImp(loadPrayerTimesForCityRepo = get())
    }
}
