package com.mbf.jordan_prayer_times_app.data.remote

/**
 * Network configuration constants
 * Centralized location for all API-related configuration
 */
object NetworkConfig {
    /*
     * No token: the data repository is public and read from raw.githubusercontent.com.
     * A token built into the app was revoked by GitHub secret scanning once a build
     * containing it was pushed, so the apps no longer carry one.
     */

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
