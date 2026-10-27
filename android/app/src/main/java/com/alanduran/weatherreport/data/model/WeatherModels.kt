package com.alanduran.weatherreport.data.model

import java.time.LocalDate
import java.time.LocalTime

enum class WeatherSource { STATION, OPEN_METEO, MIXED }

data class CurrentWeather(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val pressure: Double,
    val conditionLabel: String,
    val conditionEmoji: String,
    val isDay: Boolean,
    val windSpeed: Double,
    val windDirection: Int,
    val windDirectionLabel: String,
    val tempMax: Double,
    val tempMin: Double,
    val aqi: Int?
)

data class HourlyPoint(
    val time: LocalTime,
    val temperature: Double,
    val weatherCode: Int,
    val windSpeed: Double,
    val precipitationProbability: Int
)

data class DailyPoint(
    val date: LocalDate,
    val label: String,
    val conditionLabel: String,
    val conditionEmoji: String,
    val tempMax: Double,
    val tempMin: Double,
    val precipitationProbability: Int
)

data class AirQuality(
    val aqi: Int?,
    val category: String,
    val categoryColor: Long,
    val pm25: Double?,
    val pm10: Double?,
    val ozone: Double?,
    val no2: Double?,
    val so2: Double?,
    val co: Double?
)

data class SunTimes(
    val sunrise: LocalTime?,
    val sunset: LocalTime?
)

data class UvInfo(
    val index: Double,
    val category: String
)

data class WeatherData(
    val location: String,
    val stationName: String?,
    val current: CurrentWeather,
    val hourly: List<HourlyPoint>,
    val daily: List<DailyPoint>,
    val airQuality: AirQuality?,
    val uv: UvInfo,
    val sun: SunTimes,
    val source: WeatherSource,
    val stationUpdatedAt: String?,
    val updatedAt: LocalTime
)
