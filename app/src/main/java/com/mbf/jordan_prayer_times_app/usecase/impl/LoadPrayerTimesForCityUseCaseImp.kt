package com.mbf.jordan_prayer_times_app.usecase.impl

import com.mbf.jordan_prayer_times_app.ErrorEntity
import com.mbf.jordan_prayer_times_app.ResultData
import com.mbf.jordan_prayer_times_app.data.ui.CityUiModel
import com.mbf.jordan_prayer_times_app.data.ui.InitialHomeScreenData
import com.mbf.jordan_prayer_times_app.repositories.LoadPrayerTimesForCityRepo
import com.mbf.jordan_prayer_times_app.usecase.LoadPrayerTimesForCityUseCase

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
