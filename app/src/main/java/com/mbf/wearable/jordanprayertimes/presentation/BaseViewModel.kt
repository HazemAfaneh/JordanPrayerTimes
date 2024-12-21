package com.mbf.wearable.jordanprayertimes.presentation

import androidx.lifecycle.ViewModel
import com.mbf.wearable.jordanprayertimes.ErrorEntity
import com.mbf.wearable.jordanprayertimes.ResultData

open class BaseViewModel : ViewModel() {
    fun <T> handleResult(
        result: ResultData<T>,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit
    ) {

        when (result) {
            is ResultData.Error -> {
                var errorMessage = ""
                when (result.data) {
                    is ErrorEntity.ApiError -> {
                        errorMessage = result.data.message
                            .firstOrNull { it.isNotBlank() }
                            ?.takeIf { it.isNotEmpty() }
                            ?: "Error"
                    }

                    is ErrorEntity.InternalError -> {
                        errorMessage = result.data.message.ifBlank {
                            "Error"
                        }
                    }

                    is ErrorEntity.NoConnection -> {
                        errorMessage = "Error"
                    }

                    is ErrorEntity.ValidationError -> {
                        errorMessage = result.data.message.ifBlank {
                            "Unknown Error"
                        }
                    }
                    is ErrorEntity.AuthError ->{
                        errorMessage = result.data.message
                    }

                    else -> {
                        errorMessage = "Error"
                    }
                }
                onError(errorMessage)
            }

            is ResultData.Success -> onSuccess(result.data)
            else -> {}
        }

    }

}