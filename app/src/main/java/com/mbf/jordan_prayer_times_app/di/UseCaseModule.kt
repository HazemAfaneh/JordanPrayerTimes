package com.mbf.jordan_prayer_times_app.di

import com.mbf.jordan_prayer_times_app.usecase.LoadInitialHomeScreenDataUseCase
import com.mbf.jordan_prayer_times_app.usecase.LoadPrayerTimesForCityUseCase
import com.mbf.jordan_prayer_times_app.usecase.impl.LoadInitialHomeScreenDataUseCaseImp
import com.mbf.jordan_prayer_times_app.usecase.impl.LoadPrayerTimesForCityUseCaseImp
import org.koin.dsl.module

val useCaseModule = module {
    single<LoadInitialHomeScreenDataUseCase> {
        LoadInitialHomeScreenDataUseCaseImp(loadPrayer = get())
    }

    single<LoadPrayerTimesForCityUseCase> {
        LoadPrayerTimesForCityUseCaseImp(loadPrayerTimesForCityRepo = get())
    }
}
