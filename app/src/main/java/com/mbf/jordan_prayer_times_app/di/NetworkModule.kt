package com.mbf.jordan_prayer_times_app.di

import com.mbf.jordan_prayer_times_app.data.remote.KtorSetup
import com.mbf.jordan_prayer_times_app.data.remote.NetworkConfig
import com.mbf.jordan_prayer_times_app.data.remote.datasource.CitiesRemoteDataSource
import com.mbf.jordan_prayer_times_app.data.remote.datasource.CitiesRemoteDataSourceImp
import com.mbf.jordan_prayer_times_app.data.remote.datasource.PrayerTimesRemoteDataSource
import com.mbf.jordan_prayer_times_app.data.remote.datasource.PrayerTimesRemoteDataSourceImp
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
