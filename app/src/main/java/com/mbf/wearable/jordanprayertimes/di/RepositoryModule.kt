package com.mbf.wearable.jordanprayertimes.di

import com.mbf.wearable.jordanprayertimes.repositories.LoadCitiesRepo
import com.mbf.wearable.jordanprayertimes.repositories.LoadPrayer
import com.mbf.wearable.jordanprayertimes.repositories.impl.LoadCitiesRepoImp
import com.mbf.wearable.jordanprayertimes.repositories.impl.LoadPrayerImp
import org.koin.dsl.module

val repositoryModule = module {
    single<LoadCitiesRepo> {
        LoadCitiesRepoImp(citiesRemoteDataSource = get())
    }

    single<LoadPrayer> {
        LoadPrayerImp()
    }
}
