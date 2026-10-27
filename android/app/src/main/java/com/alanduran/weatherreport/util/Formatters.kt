package com.alanduran.weatherreport.util

import java.time.LocalTime
import kotlin.math.roundToInt

fun formatHour(time: LocalTime): String = "%02d:00".format(time.hour)

fun formatClock(time: LocalTime?): String =
    time?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "--:--"

fun formatTemperature(value: Double): String = "${value.roundToInt()}\u00B0"

fun formatOneDecimal(value: Double): String = "%.1f".format(value)

fun formatWindSpeed(value: Double): String = "%.1f km/h".format(value)
