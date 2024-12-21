package com.mbf.wearable.jordanprayertimes


sealed class ResultData<T> {
    data class Success<T>(val data: T) : ResultData<T>()
    data class Error<T>(val data: ErrorEntity) : ResultData<T>()
}

inline fun <reified T, reified A> ResultData<in T>.doIfSuccess(callback: (T) -> A): ResultData<A> {

    return when (val result = this) {
        is ResultData.Success -> ResultData.Success(callback(result.data as T))
        else -> this as ResultData<A>
    }
}

inline fun <T> ResultData<T>.doIfFailure(callback: () -> ResultData<T>): ResultData<T> {

    return when (this) {
        is ResultData.Error -> {
            callback()
        }
        else -> this as ResultData.Success
    }
}