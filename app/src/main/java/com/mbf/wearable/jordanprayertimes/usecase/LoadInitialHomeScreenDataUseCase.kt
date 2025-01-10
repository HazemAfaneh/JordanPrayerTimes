package com.mbf.wearable.jordanprayertimes.usecase

import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.ui.InitialHomeScreenData

interface LoadInitialHomeScreenDataUseCase {
    suspend operator fun invoke(
    ): ResultData<InitialHomeScreenData>
}