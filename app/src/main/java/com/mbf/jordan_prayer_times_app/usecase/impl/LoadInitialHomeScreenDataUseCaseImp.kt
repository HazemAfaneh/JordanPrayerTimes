package com.mbf.jordan_prayer_times_app.usecase.impl

import com.mbf.jordan_prayer_times_app.ErrorEntity
import com.mbf.jordan_prayer_times_app.ResultData
import com.mbf.jordan_prayer_times_app.data.ui.InitialHomeScreenData
import com.mbf.jordan_prayer_times_app.repositories.LoadPrayer
import com.mbf.jordan_prayer_times_app.usecase.LoadInitialHomeScreenDataUseCase

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