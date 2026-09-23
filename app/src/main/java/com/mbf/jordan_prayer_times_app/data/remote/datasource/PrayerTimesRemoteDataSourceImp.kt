package com.mbf.jordan_prayer_times_app.data.remote.datasource

import com.mbf.jordan_prayer_times_app.ResultData
import com.mbf.jordan_prayer_times_app.data.remote.MonthlyPrayerResponse
import com.mbf.jordan_prayer_times_app.data.remote.call
import io.ktor.client.HttpClient
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import java.util.Locale

class PrayerTimesRemoteDataSourceImp(
    private val httpClient: HttpClient
) : PrayerTimesRemoteDataSource {

    override suspend fun fetchPrayerTimes(
        cityName: String,
        year: Int,
        month: Int
    ): ResultData<MonthlyPrayerResponse?> {
        val monthStr = String.format(Locale.US, "%02d", month)
        return httpClient.call {
            url("https://raw.githubusercontent.com/mbanifawaz/Jordan_Prayer_Times_API_Data/main/monthly/${year}_${monthStr}_${cityName}.json")
            method = HttpMethod.Get
        }
    }
}
