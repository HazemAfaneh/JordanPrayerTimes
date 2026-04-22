package com.mbf.wearable.jordanprayertimes.di

import com.mbf.wearable.jordanprayertimes.repositories.LoadCitiesRepo
import com.mbf.wearable.jordanprayertimes.repositories.LoadPrayer
import com.mbf.wearable.jordanprayertimes.repositories.LoadPrayerTimesForCityRepo
import com.mbf.wearable.jordanprayertimes.repositories.impl.LoadCitiesRepoImp
import com.mbf.wearable.jordanprayertimes.repositories.impl.LoadPrayerImp
import com.mbf.wearable.jordanprayertimes.repositories.impl.LoadPrayerTimesForCityRepoImp
import org.koin.dsl.module

val repositoryModule = module {
    single<LoadCitiesRepo> {
        LoadCitiesRepoImp(citiesRemoteDataSource = get())
    }

    single<LoadPrayer> {
        LoadPrayerImp()
    }

    single<LoadPrayerTimesForCityRepo> {
        LoadPrayerTimesForCityRepoImp(prayerTimesRemoteDataSource = get())
    }
}
