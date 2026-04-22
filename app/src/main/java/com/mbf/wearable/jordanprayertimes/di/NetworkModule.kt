package com.mbf.wearable.jordanprayertimes.di

import com.mbf.wearable.jordanprayertimes.data.remote.KtorSetup
import com.mbf.wearable.jordanprayertimes.data.remote.NetworkConfig
import com.mbf.wearable.jordanprayertimes.data.remote.datasource.CitiesRemoteDataSource
import com.mbf.wearable.jordanprayertimes.data.remote.datasource.CitiesRemoteDataSourceImp
import com.mbf.wearable.jordanprayertimes.data.remote.datasource.PrayerTimesRemoteDataSource
import com.mbf.wearable.jordanprayertimes.data.remote.datasource.PrayerTimesRemoteDataSourceImp
import io.ktor.client.HttpClient
import org.koin.dsl.module

/**
 * Network module for dependency injection
 * Provides HttpClient and network-related dependencies
 */
val networkModule = module {

    /**
     * Provides KtorSetup instance
     * Configures HTTP client with logging, timeouts, and retry logic
     * Uses NetworkConfig for centralized configuration
     */
    single {
        KtorSetup(
            enableLogging = true, // Enable logging for debugging
            githubToken = NetworkConfig.GITHUB_TOKEN
        )
    }

    /**
     * Provides HttpClient instance
     * Configured with all necessary plugins for API communication
     */
    single<HttpClient> {
        val ktorSetup: KtorSetup = get()
        ktorSetup.getHttpClient()
    }

    /**
     * Provides CitiesRemoteDataSource implementation
     * Handles all remote data operations for cities
     */
    single<CitiesRemoteDataSource> {
        CitiesRemoteDataSourceImp(httpClient = get())
    }

    single<PrayerTimesRemoteDataSource> {
        PrayerTimesRemoteDataSourceImp(httpClient = get())
    }
}
