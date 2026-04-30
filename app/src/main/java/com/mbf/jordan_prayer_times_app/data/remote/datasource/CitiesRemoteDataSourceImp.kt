package com.mbf.jordan_prayer_times_app.data.remote.datasource

import com.mbf.jordan_prayer_times_app.BuildConfig
import com.mbf.jordan_prayer_times_app.ResultData
import com.mbf.jordan_prayer_times_app.data.remote.CitiesResponse
import com.mbf.jordan_prayer_times_app.data.remote.call
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.HttpMethod

class CitiesRemoteDataSourceImp(
    private val httpClient: HttpClient
) : CitiesRemoteDataSource {

    override suspend fun fetchCities(): ResultData<CitiesResponse?> {
        return httpClient.call {
            url("https://api.github.com/repos/mbanifawaz/Jordan_Prayer_Times_API_Data/contents/cities/cities.json")
            method = HttpMethod.Get
            header("Authorization", "Bearer ${BuildConfig.GITHUB_TOKEN}")
            header("Accept", "application/vnd.github.raw+json")
        }
    }
}
