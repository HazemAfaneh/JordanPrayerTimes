package com.mbf.wearable.jordanprayertimes.repositories

import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel
import com.mbf.wearable.jordanprayertimes.data.ui.InitialHomeScreenData

interface LoadPrayerTimesForCityRepo {
    suspend operator fun invoke(city: CityUiModel): ResultData<InitialHomeScreenData>
}
