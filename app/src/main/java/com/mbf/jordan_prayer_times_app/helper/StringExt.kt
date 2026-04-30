package com.mbf.jordan_prayer_times_app.helper

import java.util.Locale

fun String.to12hFormat(): String {
    val parts = split(":")
    if (parts.size < 2) return this
    val h = parts[0].trim().toIntOrNull() ?: return this
    val m = parts[1].trim().toIntOrNull() ?: return this
    val hour12 = when { h == 0 -> 12; h > 12 -> h - 12; else -> h }
    return String.format(Locale.US, "%d:%02d", hour12, m)
}
