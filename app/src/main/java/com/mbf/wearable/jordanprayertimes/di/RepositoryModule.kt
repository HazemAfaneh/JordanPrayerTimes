package com.mbf.wearable.jordanprayertimes.di

import com.mbf.wearable.jordanprayertimes.data.local.CityPreferences
import com.mbf.wearable.jordanprayertimes.data.local.MonthlyPrayerCache
import com.mbf.wearable.jordanprayertimes.repositories.LoadCitiesRepo
import com.mbf.wearable.jordanprayertimes.repositories.LoadPrayer
import com.mbf.wearable.jordanprayertimes.repositories.LoadPrayerTimesForCityRepo
import com.mbf.wearable.jordanprayertimes.repositories.impl.LoadCitiesRepoImp
import com.mbf.wearable.jordanprayertimes.repositories.impl.LoadPrayerImp
import com.mbf.wearable.jordanprayertimes.repositories.impl.LoadPrayerTimesForCityRepoImp
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single { CityPreferences(androidContext()) }
    single { MonthlyPrayerCache(androidContext()) }

    single<LoadCitiesRepo> {
        LoadCitiesRepoImp(citiesRemoteDataSource = get())
    }

    single<LoadPrayer> {
        LoadPrayerImp()
    }

    single<LoadPrayerTimesForCityRepo> {
        LoadPrayerTimesForCityRepoImp(
            prayerTimesRemoteDataSource = get(),
            monthlyPrayerCache = get()
        )
    }
}
