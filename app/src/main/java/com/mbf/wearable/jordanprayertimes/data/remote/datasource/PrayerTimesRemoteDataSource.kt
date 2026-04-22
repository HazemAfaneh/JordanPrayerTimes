package com.mbf.wearable.jordanprayertimes.data.remote.datasource

import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.remote.MonthlyPrayerResponse

interface PrayerTimesRemoteDataSource {
    suspend fun fetchPrayerTimes(
        cityName: String,
        year: Int,
        month: Int
    ): ResultData<MonthlyPrayerResponse?>
}
