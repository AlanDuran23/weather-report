package com.alanduran.weatherreport.util

data class WeatherCondition(
    val label: String,
    val emoji: String,
    val rainy: Boolean
)

fun weatherConditionFromCode(code: Int?): WeatherCondition = when (code ?: 3) {
    0 -> WeatherCondition("Despejado", "\u2600\uFE0F", false)
    1 -> WeatherCondition("Mayormente despejado", "\uD83C\uDF24\uFE0F", false)
    2 -> WeatherCondition("Parcialmente nublado", "\u26C5", false)
    3 -> WeatherCondition("Nublado", "\u2601\uFE0F", false)
    45, 48 -> WeatherCondition("Niebla", "\uD83C\uDF2B\uFE0F", false)
    51, 53, 55 -> WeatherCondition("Llovizna", "\uD83C\uDF26\uFE0F", true)
    56, 57 -> WeatherCondition("Llovizna helada", "\uD83C\uDF27\uFE0F", true)
    61, 63, 65 -> WeatherCondition("Lluvia", "\uD83C\uDF27\uFE0F", true)
    66, 67 -> WeatherCondition("Lluvia helada", "\uD83C\uDF27\uFE0F", true)
    71, 73, 75, 77 -> WeatherCondition("Nieve", "\uD83C\uDF28\uFE0F", true)
    80, 81, 82 -> WeatherCondition("Chubascos", "\uD83C\uDF27\uFE0F", true)
    85, 86 -> WeatherCondition("Chubascos de nieve", "\uD83C\uDF28\uFE0F", true)
    95 -> WeatherCondition("Tormenta", "\u26C8\uFE0F", true)
    96, 99 -> WeatherCondition("Tormenta con granizo", "\u26C8\uFE0F", true)
    else -> WeatherCondition("Nublado", "\u2601\uFE0F", false)
}

/** Deriva una condicion a partir del texto libre de observaciones de la estacion. */
fun weatherConditionFromText(text: String?): WeatherCondition? {
    val value = text?.lowercase()?.trim().orEmpty()
    if (value.isEmpty()) return null
    return when {
        value.contains("tormenta") -> WeatherCondition("Tormenta", "\u26C8\uFE0F", true)
        value.contains("chubasc") -> WeatherCondition("Chubascos", "\uD83C\uDF27\uFE0F", true)
        value.contains("lluvia") || value.contains("lloviz") ->
            WeatherCondition("Lluvia", "\uD83C\uDF27\uFE0F", true)
        value.contains("niebla") || value.contains("neblina") ->
            WeatherCondition("Niebla", "\uD83C\uDF2B\uFE0F", false)
        value.contains("parcial") -> WeatherCondition("Parcialmente nublado", "\u26C5", false)
        value.contains("nublado") -> WeatherCondition("Nublado", "\u2601\uFE0F", false)
        value.contains("despejado") || value.contains("soleado") ->
            WeatherCondition("Despejado", "\u2600\uFE0F", false)
        else -> null
    }
}

fun uvCategory(index: Double): String = when {
    index < 3 -> "D\u00E9bil"
    index < 6 -> "Moderado"
    index < 8 -> "Alto"
    index < 11 -> "Muy alto"
    else -> "Extremo"
}

data class AqiCategory(val label: String, val color: Long)

fun aqiCategory(aqi: Int?): AqiCategory = when {
    aqi == null -> AqiCategory("Sin datos", 0xFF9E9E9E)
    aqi <= 50 -> AqiCategory("Bueno", 0xFF43E08B)
    aqi <= 100 -> AqiCategory("Moderado", 0xFFFFB33D)
    aqi <= 150 -> AqiCategory("Da\u00F1ino para grupos sensibles", 0xFFFF8A3D)
    aqi <= 200 -> AqiCategory("Da\u00F1ino", 0xFFFF5252)
    aqi <= 300 -> AqiCategory("Muy da\u00F1ino", 0xFFB44DD8)
    else -> AqiCategory("Peligroso", 0xFF8B1A4B)
}
