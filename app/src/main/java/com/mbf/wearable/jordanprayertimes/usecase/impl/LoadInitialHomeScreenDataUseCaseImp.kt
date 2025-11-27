package com.mbf.wearable.jordanprayertimes.usecase.impl

import com.mbf.wearable.jordanprayertimes.ErrorEntity
import com.mbf.wearable.jordanprayertimes.ResultData
import com.mbf.wearable.jordanprayertimes.data.ui.InitialHomeScreenData
import com.mbf.wearable.jordanprayertimes.repositories.LoadPrayer
import com.mbf.wearable.jordanprayertimes.usecase.LoadInitialHomeScreenDataUseCase

/**
 * Use case for loading initial home screen data
 * Only loads prayer times - cities are loaded separately in SettingsViewModel
 */
class LoadInitialHomeScreenDataUseCaseImp(
    private val loadPrayer: LoadPrayer
) : LoadInitialHomeScreenDataUseCase {

    override suspend fun invoke(): ResultData<InitialHomeScreenData> {
        return try {
            when (val prayersResult = loadPrayer()) {
                is ResultData.Success -> {
                    ResultData.Success(
                        InitialHomeScreenData(
                            prayers = prayersResult.data
                        )
                    )
                }

                is ResultData.Error -> {
                    ResultData.Error(prayersResult.data)
                }
            }
        } catch (e: Exception) {
            ResultData.Error(ErrorEntity.Unknown)
        }
    }
}