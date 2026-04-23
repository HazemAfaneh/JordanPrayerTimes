package com.mbf.jordan_prayer_times_app.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * Ktor HTTP Client configuration for Jordan Prayer Times API
 * Handles GitHub API requests with proper retry logic, timeouts, and logging
 * Uses OkHttp engine for better Android compatibility
 */
class KtorSetup(
    private val enableLogging: Boolean = true,
    private val githubToken: String = NetworkConfig.GITHUB_TOKEN
) {
    companion object {
        private const val TAG = "KtorSetup"
    }

    /**
     * Creates and configures the HttpClient with all necessary plugins
     */
    fun getHttpClient(): HttpClient {
        return HttpClient(OkHttp) {
            // OkHttp engine configuration
            engine {
                config {
                    connectTimeout(NetworkConfig.Timeouts.CONNECT, TimeUnit.MILLISECONDS)
                    readTimeout(NetworkConfig.Timeouts.REQUEST, TimeUnit.MILLISECONDS)
                    writeTimeout(NetworkConfig.Timeouts.REQUEST, TimeUnit.MILLISECONDS)
                    retryOnConnectionFailure(true)
                }
            }

            // JSON Content Negotiation
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = enableLogging
                        encodeDefaults = true
                        coerceInputValues = true
                    }
                )
            }

            // Logging Plugin
            if (enableLogging) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            Log.d(TAG, message)
                        }
                    }
                    level = LogLevel.ALL
                }
            }

            // Retry Plugin
            install(HttpRequestRetry) {
                maxRetries = NetworkConfig.Retry.MAX_RETRIES

                retryIf { _, response ->
                    // Retry on specific status codes
                    response.status in listOf(
                        HttpStatusCode.RequestTimeout,
                        HttpStatusCode.TooManyRequests,
                        HttpStatusCode.ServiceUnavailable,
                        HttpStatusCode.GatewayTimeout
                    )
                }

                retryOnExceptionIf { _, cause ->
                    // Retry on network exceptions
                    cause is java.net.SocketTimeoutException ||
                            cause is java.net.UnknownHostException ||
                            cause is java.io.IOException
                }

                delayMillis { retry ->
                    // Exponential backoff
                    NetworkConfig.Retry.BASE_DELAY_MS * (1 shl retry)
                }

                modifyRequest { request ->
                    // Log retry attempts
                    if (enableLogging) {
                        Log.d(TAG, "Retrying request: ${request.url}")
                    }
                }
            }

            // Default Request Configuration
            defaultRequest {
                // Set common headers for all requests
                header(HttpHeaders.UserAgent, "JordanPrayerTimes-WearOS/1.0")
            }

            // Response validation
            expectSuccess = false
        }
    }

    /**
     * Helper to build full GitHub API URLs
     */
    fun buildGithubApiUrl(path: String): String {
        return "${NetworkConfig.GITHUB_BASE_URL}/${NetworkConfig.REPO_PATH}/$path"
    }
}
