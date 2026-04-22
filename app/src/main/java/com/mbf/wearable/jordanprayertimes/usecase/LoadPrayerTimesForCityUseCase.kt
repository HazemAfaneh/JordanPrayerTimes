package com.mbf.wearable.jordanprayertimes.usecase

import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.InitialHomeScreenData

interface LoadPrayerTimesForCityUseCase {
    suspend operator fun invoke(city: CityUiModel): ResultData<InitialHomeScreenData>
}
