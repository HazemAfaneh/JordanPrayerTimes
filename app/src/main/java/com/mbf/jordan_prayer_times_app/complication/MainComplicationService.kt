package com.mbf.jordan_prayer_times_app.complication

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.mbf.jordan_prayer_times_app.data.local.CityPreferences
import com.mbf.jordan_prayer_times_app.data.local.MonthlyPrayerCache
import com.mbf.jordan_prayer_times_app.data.remote.findTodayPrayers
import com.mbf.jordan_prayer_times_app.data.remote.nextPrayerInfo
import com.mbf.jordan_prayer_times_app.data.remote.toPrayerUiModels
import com.mbf.jordan_prayer_times_app.helper.to12hFormat
import com.mbf.jordan_prayer_times_app.presentation.MainActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar

class MainComplicationService : SuspendingComplicationDataSourceService(), KoinComponent {

    private val cityPreferences: CityPreferences by inject()
    private val monthlyPrayerCache: MonthlyPrayerCache by inject()

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        if (type != ComplicationType.SHORT_TEXT) return null
        return buildComplicationData(prayerName = "المغرب", prayerTime = "6:45")
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData {
        val next = resolveNextPrayer()
        return if (next != null) {
            buildComplicationData(prayerName = next.first, prayerTime = next.second)
        } else {
            buildComplicationData(prayerName = "الصلاة", prayerTime = "--:--")
        }
    }

    private fun resolveNextPrayer(): Pair<String, String>? {
        val city = cityPreferences.getSavedCity() ?: return null
        val cal = Calendar.getInstance()
        val monthlyData = monthlyPrayerCache.get(
            cityName = city.name,
            year = cal.get(Calendar.YEAR),
            month = cal.get(Calendar.MONTH) + 1
        ) ?: return null

        val prayers = monthlyData.prayerData.findTodayPrayers()?.toPrayerUiModels()
            ?: return null

        val (nextName, _) = prayers.nextPrayerInfo()
        val nextPrayer = prayers.find { it.name == nextName } ?: return null
        return nextName to nextPrayer.prayerTime.to12hFormat()
    }

    private fun buildComplicationData(prayerName: String, prayerTime: String): ShortTextComplicationData {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val tapAction = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(prayerTime).build(),
            contentDescription = PlainComplicationText.Builder("$prayerName $prayerTime").build()
        )
            .setTitle(PlainComplicationText.Builder(prayerName).build())
            .setTapAction(tapAction)
            .build()
    }
}
