package com.mbf.wearable.jordanprayertimes.data.ui

/**
 * Initial home screen data model
 * Contains only prayer-related data
 * Cities are loaded separately in SettingsViewModel
 */
data class InitialHomeScreenData(
    val prayers: List<PrayerUiModel> = emptyList(),
    val currentDate: String = java.text.SimpleDateFormat(
        "EEEE, yyyy-MM-dd",
        java.util.Locale.getDefault()
    ).format(java.util.Date()),
    val nextPray: String = "العشاء",
    val nextPrayTimeIn: String = "11:20",
    val nextPrayTime: Long = System.currentTimeMillis() + (60 * 60 * 1000L),
    val currentCity: CityUiModel = CityUiModel(name = "Amman", id = 1, isSelected = true)
)
