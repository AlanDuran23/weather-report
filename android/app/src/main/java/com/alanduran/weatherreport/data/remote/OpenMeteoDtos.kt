package com.alanduran.weatherreport.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenMeteoForecastResponse(
    val timezone: String? = null,
    val current: CurrentDto? = null,
    val hourly: HourlyDto? = null,
    val daily: DailyDto? = null
)

@Serializable
data class CurrentDto(
    val time: String? = null,
    @SerialName("temperature_2m") val temperature: Double? = null,
    @SerialName("relative_humidity_2m") val humidity: Int? = null,
    @SerialName("apparent_temperature") val apparentTemperature: Double? = null,
    @SerialName("is_day") val isDay: Int? = null,
    @SerialName("weather_code") val weatherCode: Int? = null,
    @SerialName("wind_speed_10m") val windSpeed: Double? = null,
    @SerialName("wind_direction_10m") val windDirection: Int? = null,
    @SerialName("surface_pressure") val surfacePressure: Double? = null,
    @SerialName("pressure_msl") val pressureMsl: Double? = null
)

@Serializable
data class HourlyDto(
    val time: List<String> = emptyList(),
    @SerialName("temperature_2m") val temperature: List<Double?> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int?> = emptyList(),
    @SerialName("wind_speed_10m") val windSpeed: List<Double?> = emptyList(),
    @SerialName("precipitation_probability") val precipitation: List<Int?> = emptyList()
)

@Serializable
data class DailyDto(
    val time: List<String> = emptyList(),
    @SerialName("weather_code") val weatherCode: List<Int?> = emptyList(),
    @SerialName("temperature_2m_max") val tempMax: List<Double?> = emptyList(),
    @SerialName("temperature_2m_min") val tempMin: List<Double?> = emptyList(),
    @SerialName("precipitation_probability_max") val precipitation: List<Int?> = emptyList(),
    @SerialName("uv_index_max") val uvIndexMax: List<Double?> = emptyList(),
    val sunrise: List<String?> = emptyList(),
    val sunset: List<String?> = emptyList()
)

@Serializable
data class AirQualityResponse(
    val current: AirQualityCurrent? = null
)

@Serializable
data class AirQualityCurrent(
    val time: String? = null,
    @SerialName("us_aqi") val usAqi: Int? = null,
    @SerialName("pm2_5") val pm25: Double? = null,
    val pm10: Double? = null,
    val ozone: Double? = null,
    @SerialName("nitrogen_dioxide") val no2: Double? = null,
    @SerialName("sulphur_dioxide") val so2: Double? = null,
    @SerialName("carbon_monoxide") val co: Double? = null
)
