package com.mbf.jordan_prayer_times_app.usecase

import com.mbf.jordan_prayer_times_app.ResultData
import com.mbf.jordan_prayer_times_app.data.ui.CityUiModel
import com.mbf.jordan_prayer_times_app.data.ui.InitialHomeScreenData

interface LoadPrayerTimesForCityUseCase {
    suspend operator fun invoke(city: CityUiModel): ResultData<InitialHomeScreenData>
}
