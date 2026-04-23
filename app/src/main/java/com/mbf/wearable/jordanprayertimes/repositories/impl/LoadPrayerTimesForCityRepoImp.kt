package com.mbf.wearable.jordanprayertimes.repositories.impl

import com.mbf.wearable.jordanprayertimes.ErrorEntity
import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.local.MonthlyPrayerCache
import com.mbf.wearable.jordanprayertimes.data.remote.MonthlyPrayerResponse
import com.mbf.wearable.jordanprayertimes.data.remote.datasource.PrayerTimesRemoteDataSource
import com.mbf.wearable.jordanprayertimes.data.remote.findTodayPrayers
import com.mbf.wearable.jordanprayertimes.data.remote.nextPrayerInfo
import com.mbf.wearable.jordanprayertimes.data.remote.toPrayerUiModels
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.InitialHomeScreenData
import com.mbf.wearable.jordanprayertimes.repositories.LoadPrayerTimesForCityRepo
import java.util.Calendar

class LoadPrayerTimesForCityRepoImp(
    private val prayerTimesRemoteDataSource: PrayerTimesRemoteDataSource,
    private val monthlyPrayerCache: MonthlyPrayerCache
) : LoadPrayerTimesForCityRepo {

    override suspend fun invoke(city: CityUiModel): ResultData<InitialHomeScreenData> {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1

        val monthlyData = getMonthlyData(city.name, year, month)
            ?: return ResultData.Error(ErrorEntity.InternalError("تعذّر تحميل بيانات ${city.name}"))

        val prayerData = monthlyData.prayerData
        if (prayerData.isEmpty()) {
            return ResultData.Error(ErrorEntity.InternalError("لا توجد بيانات صلاة لـ ${city.name}"))
        }

        val todayPrayers = prayerData.findTodayPrayers()
            ?: return ResultData.Error(ErrorEntity.InternalError("لا توجد بيانات لليوم"))

        val prayers = todayPrayers.toPrayerUiModels()
        val (nextPrayName, nextPrayTimeMs) = prayers.nextPrayerInfo()

        return ResultData.Success(
            InitialHomeScreenData(
                prayers = prayers,
                nextPray = nextPrayName,
                nextPrayTime = nextPrayTimeMs,
                currentCity = city
            )
        )
    }

    private suspend fun getMonthlyData(
        cityName: String,
        year: Int,
        month: Int
    ): MonthlyPrayerResponse? {
        monthlyPrayerCache.get(cityName, year, month)?.let { return it }

        return when (val result = prayerTimesRemoteDataSource.fetchPrayerTimes(cityName, year, month)) {
            is ResultData.Success -> result.data?.also {
                monthlyPrayerCache.put(cityName, year, month, it)
            }
            is ResultData.Error -> null
        }
    }
}
