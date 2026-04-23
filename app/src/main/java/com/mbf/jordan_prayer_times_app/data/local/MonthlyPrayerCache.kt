package com.mbf.jordan_prayer_times_app.data.local

import android.content.Context
import com.mbf.jordan_prayer_times_app.data.remote.MonthlyPrayerResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MonthlyPrayerCache(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun get(cityName: String, year: Int, month: Int): MonthlyPrayerResponse? {
        val raw = prefs.getString(key(cityName, year, month), null) ?: return null
        return try {
            json.decodeFromString<MonthlyPrayerResponse>(raw)
        } catch (_: Exception) {
            null
        }
    }

    fun put(cityName: String, year: Int, month: Int, data: MonthlyPrayerResponse) {
        prefs.edit()
            .clear()
            .putString(key(cityName, year, month), json.encodeToString(data))
            .apply()
    }

    private fun key(cityName: String, year: Int, month: Int) = "${cityName}_${year}_${month}"

    companion object {
        private const val PREFS_NAME = "monthly_prayer_cache"
    }
}
