package com.mbf.jordan_prayer_times_app.usecase

import com.mbf.jordan_prayer_times_app.ResultData
import com.mbf.jordan_prayer_times_app.data.ui.InitialHomeScreenData

interface LoadInitialHomeScreenDataUseCase {
    suspend operator fun invoke(
    ): ResultData<InitialHomeScreenData>
}