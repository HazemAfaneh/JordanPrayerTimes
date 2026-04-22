package com.mbf.wearable.jordanprayertimes.usecase.impl

import com.mbf.wearable.jordanprayertimes.ErrorEntity
import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.InitialHomeScreenData
import com.mbf.wearable.jordanprayertimes.repositories.LoadPrayerTimesForCityRepo
import com.mbf.wearable.jordanprayertimes.usecase.LoadPrayerTimesForCityUseCase

class LoadPrayerTimesForCityUseCaseImp(
    private val loadPrayerTimesForCityRepo: LoadPrayerTimesForCityRepo
) : LoadPrayerTimesForCityUseCase {

    override suspend fun invoke(city: CityUiModel): ResultData<InitialHomeScreenData> {
        return try {
            loadPrayerTimesForCityRepo(city)
        } catch (e: Exception) {
            ResultData.Error(ErrorEntity.Unknown)
        }
    }
}
