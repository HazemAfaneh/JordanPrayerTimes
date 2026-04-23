package com.mbf.wearable.jordanprayertimes.data.local

import android.content.Context
import com.mbf.wearable.jordanprayertimes.data.ui.CityUiModel

class CityPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveCity(city: CityUiModel) {
        prefs.edit()
            .putInt(KEY_CITY_ID, city.id)
            .putString(KEY_CITY_NAME, city.name)
            .apply()
    }

    fun getSavedCity(): CityUiModel? {
        val id = prefs.getInt(KEY_CITY_ID, -1)
        val name = prefs.getString(KEY_CITY_NAME, null)
        return if (id != -1 && name != null) CityUiModel(id = id, name = name, isSelected = true)
        else null
    }

    fun hasSavedCity(): Boolean = prefs.getInt(KEY_CITY_ID, -1) != -1

    fun saveNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean =
        prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    companion object {
        private const val PREFS_NAME = "city_prefs"
        private const val KEY_CITY_ID = "city_id"
        private const val KEY_CITY_NAME = "city_name"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }
}
