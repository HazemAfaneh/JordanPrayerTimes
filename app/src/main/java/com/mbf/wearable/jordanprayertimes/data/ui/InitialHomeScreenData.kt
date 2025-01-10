package com.mbf.wearable.jordanprayertimes.data.ui

data class InitialHomeScreenData(
    val cities: List<CityUiModel> = emptyList(),
    val prayers: List<PrayerUiModel> = emptyList(),
    val currentDate: String = java.text.SimpleDateFormat(
        "EEEE, yyyy-MM-dd",
        java.util.Locale.getDefault()
    ).format(java.util.Date()),
    val nextPray:String = "Ishaa",
    val nextPrayTimeIn:String ="11:20",
    val nextPrayTime:Long =System.currentTimeMillis() + (60 * 60 * 1000L),
    val currentCity:CityUiModel = CityUiModel(name = "Amman", id = 1, isSelected = true)
)
