package com.mbf.wearable.jordanprayertimes.repositories

import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.ui.PrayerUiModel

interface LoadPrayer {
    suspend operator fun invoke(
    ): ResultData<List<PrayerUiModel>>
}