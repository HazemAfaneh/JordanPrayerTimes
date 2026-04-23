package com.mbf.jordan_prayer_times_app.data.remote

import com.mbf.jordan_prayer_times_app.data.ui.PrayerUiModel
import kotlinx.serialization.Serializable
import java.util.Calendar
import java.util.Locale

@Serializable
data class MonthlyPrayerResponse(
    val month: String = "",
    val city: String = "",
    val prayerData: List<DailyPrayerRemoteModel> = emptyList()
)

@Serializable
data class DailyPrayerRemoteModel(
    val date: String = "",
    val fajr: String = "",
    val sunrise: String = "",
    val dhuhr: String = "",
    val asr: String = "",
    val maghrib: String = "",
    val isha: String = ""
)

// Matches today's entry by "DD/MM/YYYY" date string, falls back to array index.
fun List<DailyPrayerRemoteModel>.findTodayPrayers(): DailyPrayerRemoteModel? {
    val cal = Calendar.getInstance()
    val todayStr = String.format(
        Locale.US, "%02d/%02d/%d",
        cal.get(Calendar.DAY_OF_MONTH),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.YEAR)
    )
    return find { it.date == todayStr } ?: getOrNull(cal.get(Calendar.DAY_OF_MONTH) - 1)
}

// Converts raw 12-hour prayer times to PrayerUiModel with 24-hour display times.
// The API returns fajr/sunrise in AM and asr/maghrib/isha in PM without an explicit marker,
// so we enforce ascending order: if a time is <= the previous, it must be PM — add 12 hours.
fun DailyPrayerRemoteModel.toPrayerUiModels(): List<PrayerUiModel> {
    val rawTimes = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
    val names = listOf("الفجر", "الشروق", "الظهر", "العصر", "المغرب", "العشاء")

    var lastMinutes = 0
    return rawTimes.mapIndexedNotNull { index, timeStr ->
        if (timeStr.isEmpty()) return@mapIndexedNotNull null
        val parts = timeStr.split(":")
        if (parts.size < 2) return@mapIndexedNotNull null
        val h = parts[0].trim().toIntOrNull() ?: return@mapIndexedNotNull null
        val m = parts[1].trim().toIntOrNull() ?: return@mapIndexedNotNull null

        var totalMinutes = h * 60 + m
        if (totalMinutes <= lastMinutes && h < 12) totalMinutes += 12 * 60
        lastMinutes = totalMinutes

        PrayerUiModel(
            id = index + 1,
            name = names[index],
            prayerTime = String.format(Locale.US, "%02d:%02d", totalMinutes / 60, totalMinutes % 60)
        )
    }
}

// Finds the next upcoming prayer based on the 24-hour times produced by toPrayerUiModels().
fun List<PrayerUiModel>.nextPrayerInfo(): Pair<String, Long> {
    val now = System.currentTimeMillis()
    val midnight = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val next = this.mapNotNull { prayer ->
        val parts = prayer.prayerTime.split(":")
        if (parts.size < 2) return@mapNotNull null
        val h = parts[0].trim().toIntOrNull() ?: return@mapNotNull null
        val m = parts[1].trim().toIntOrNull() ?: return@mapNotNull null
        val prayerMs = midnight + (h * 3600L + m * 60L) * 1000L
        if (prayerMs > now) prayer.name to prayerMs else null
    }.minByOrNull { (_, ms) -> ms }

    return next ?: ((firstOrNull()?.name ?: "الفجر") to (midnight + 24 * 3600 * 1000L))
}
