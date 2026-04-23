package com.mbf.jordan_prayer_times_app.data.remote.datasource

import com.mbf.jordan_prayer_times_app.ResultData
import com.mbf.jordan_prayer_times_app.data.remote.MonthlyPrayerResponse

interface PrayerTimesRemoteDataSource {
    suspend fun fetchPrayerTimes(
        cityName: String,
        year: Int,
        month: Int
    ): ResultData<MonthlyPrayerResponse?>
}
