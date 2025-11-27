package com.mbf.wearable.jordanprayertimes.data.remote

import com.mbf.wearable.jordanprayertimes.ErrorEntity
import com.mbf.wearable.jordanprayertimes.ResultData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Extension function for making HTTP calls with proper error handling
 * Maps network errors to ErrorEntity
 */
suspend inline fun <reified T> HttpClient.call(
    block: HttpRequestBuilder.() -> Unit
): ResultData<T?> = try {
    val response = request(block)

    when {
        response.status.isSuccess() -> try {
            val data = response.body<T>()
            ResultData.Success(data)
        } catch (e: Exception) {
            ResultData.Error(
                ErrorEntity.InternalError("Failed to parse response: ${e.message}")
            )
        }

        response.status == HttpStatusCode.Unauthorized ||
                response.status == HttpStatusCode.Forbidden -> {
            ResultData.Error(
                ErrorEntity.AuthError("Authentication failed")
            )
        }

        response.status == HttpStatusCode.RequestTimeout -> {
            ResultData.Error(
                ErrorEntity.InternalError("Request timeout")
            )
        }

        response.status.value in 400..499 -> {
            ResultData.Error(
                ErrorEntity.ApiError(
                    code = response.status.value,
                    message = listOf("Client error: ${response.status.description}")
                )
            )
        }

        response.status.value in 500..599 -> {
            ResultData.Error(
                ErrorEntity.ApiError(
                    code = response.status.value,
                    message = listOf("Server error: ${response.status.description}")
                )
            )
        }

        else -> ResultData.Error(ErrorEntity.Unknown)
    }
} catch (e: SocketTimeoutException) {
    ResultData.Error(
        ErrorEntity.InternalError("Request timed out: ${e.message}")
    )
} catch (e: UnknownHostException) {
    ResultData.Error(ErrorEntity.NoConnection)
} catch (e: Exception) {
    val message = e.message ?: ""
    when {
        message.contains("Unable to resolve host", ignoreCase = true) ||
                message.contains("host could not be found", ignoreCase = true) ||
                message.contains("No address associated", ignoreCase = true) -> {
            ResultData.Error(ErrorEntity.NoConnection)
        }

        message.contains("timed out", ignoreCase = true) -> {
            ResultData.Error(
                ErrorEntity.InternalError("Request timed out")
            )
        }

        else -> ResultData.Error(
            ErrorEntity.InternalError(message.ifEmpty { "Unknown error occurred" })
        )
    }
}
