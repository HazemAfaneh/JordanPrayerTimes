package com.mbf.wearable.jordanprayertimes.repositories.impl

import com.mbf.wearable.jordanprayertimes.ErrorEntity
import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.remote.datasource.PrayerTimesRemoteDataSource
import com.mbf.wearable.jordanprayertimes.data.remote.findTodayPrayers
import com.mbf.wearable.jordanprayertimes.data.remote.nextPrayerInfo
import com.mbf.wearable.jordanprayertimes.data.remote.toPrayerUiModels
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.InitialHomeScreenData
import com.mbf.wearable.jordanprayertimes.repositories.LoadPrayerTimesForCityRepo
import java.util.Calendar

class LoadPrayerTimesForCityRepoImp(
    private val prayerTimesRemoteDataSource: PrayerTimesRemoteDataSource
) : LoadPrayerTimesForCityRepo {

    override suspend fun invoke(city: CityUiModel): ResultData<InitialHomeScreenData> {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1

        return when (val result = prayerTimesRemoteDataSource.fetchPrayerTimes(
            cityName = city.name,
            year = year,
            month = month
        )) {
            is ResultData.Success -> {
                val prayerData = result.data?.prayerData
                if (prayerData.isNullOrEmpty()) {
                    return ResultData.Error(ErrorEntity.InternalError("No prayer data for ${city.name}"))
                }

                val todayPrayers = prayerData.findTodayPrayers()
                    ?: return ResultData.Error(ErrorEntity.InternalError("No data for today"))

                val prayers = todayPrayers.toPrayerUiModels()
                val (nextPrayName, nextPrayTimeMs) = prayers.nextPrayerInfo()

                ResultData.Success(
                    InitialHomeScreenData(
                        prayers = prayers,
                        nextPray = nextPrayName,
                        nextPrayTime = nextPrayTimeMs,
                        currentCity = city
                    )
                )
            }

            is ResultData.Error -> ResultData.Error(result.data)
        }
    }
}
