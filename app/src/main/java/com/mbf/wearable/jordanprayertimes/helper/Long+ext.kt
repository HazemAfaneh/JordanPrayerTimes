package com.mbf.wearable.jordanprayertimes.helper

import java.util.Locale

fun Long.timeToHoursAndMinutes():String{

    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600  // Calculate hours
    val minutes = (totalSeconds % 3600) / 60  // Calculate minutes

    return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)  // Use explicit Locale
}