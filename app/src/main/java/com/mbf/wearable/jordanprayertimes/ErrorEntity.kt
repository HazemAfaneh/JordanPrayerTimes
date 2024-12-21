package com.mbf.wearable.jordanprayertimes


sealed class ErrorEntity {
    object NoConnection : ErrorEntity()
    object Unknown : ErrorEntity()

    data class ApiError(val code: Int, val message: List<String>) : ErrorEntity()
    data class AuthError(val message: String) : ErrorEntity()
    data class InternalError(val message: String) : ErrorEntity()
    data class ValidationError(
        val message: String
    ) : ErrorEntity()

    object None : ErrorEntity()

}
