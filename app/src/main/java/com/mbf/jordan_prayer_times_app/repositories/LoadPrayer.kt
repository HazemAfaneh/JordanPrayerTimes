package com.mbf.jordan_prayer_times_app.repositories

import com.mbf.jordan_prayer_times_app.ResultData
import com.mbf.jordan_prayer_times_app.data.ui.PrayerUiModel

interface LoadPrayer {
    suspend operator fun invoke(
    ): ResultData<List<PrayerUiModel>>
}