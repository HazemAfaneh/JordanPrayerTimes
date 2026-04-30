package com.mbf.jordan_prayer_times_app.data.remote

/**
 * Network configuration constants
 * Centralized location for all API-related configuration
 */
object NetworkConfig {
    /**
     * GitHub Personal Access Token
     * Used for authenticated requests to GitHub API
     * Note: In production, this should be stored securely (e.g., in BuildConfig, encrypted storage, or backend)
     */
    const val GITHUB_TOKEN =
        "REMOVED_TOKEN"

    /**
     * Base URL for GitHub API
     */
    const val GITHUB_BASE_URL = "https://api.github.com"

    /**
     * Repository path for prayer times data
     */
    const val REPO_PATH = "repos/mbanifawaz/Jordan_Prayer_Times_API_Data/contents"

    /**
     * API endpoints
     */
    object Endpoints {
        const val CITIES = "cities/cities.json"
        const val PRAYERS_PREFIX = "prayers/"

        /**
         * Get prayer times endpoint for a specific city
         */
        fun getPrayerTimes(cityName: String): String {
            return "$PRAYERS_PREFIX$cityName.json"
        }
    }

    /**
     * Timeout configurations (in milliseconds)
     */
    object Timeouts {
        const val REQUEST = 30_000L
        const val CONNECT = 30_000L
        const val SOCKET = 30_000L
    }

    /**
     * Retry configurations
     */
    object Retry {
        const val MAX_RETRIES = 3
        const val BASE_DELAY_MS = 1000L
    }
}
