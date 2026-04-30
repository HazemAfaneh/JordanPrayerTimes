package com.mbf.jordan_prayer_times_app.di

import com.mbf.jordan_prayer_times_app.data.local.CityPreferences
import com.mbf.jordan_prayer_times_app.data.local.MonthlyPrayerCache
import com.mbf.jordan_prayer_times_app.repositories.LoadCitiesRepo
import com.mbf.jordan_prayer_times_app.repositories.LoadPrayer
import com.mbf.jordan_prayer_times_app.repositories.LoadPrayerTimesForCityRepo
import com.mbf.jordan_prayer_times_app.repositories.impl.LoadCitiesRepoImp
import com.mbf.jordan_prayer_times_app.repositories.impl.LoadPrayerImp
import com.mbf.jordan_prayer_times_app.repositories.impl.LoadPrayerTimesForCityRepoImp
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
