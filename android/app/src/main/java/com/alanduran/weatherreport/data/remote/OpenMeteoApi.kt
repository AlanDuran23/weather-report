package com.alanduran.weatherreport.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoForecastApi {

    @GET("v1/forecast")
    suspend fun forecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("hourly") hourly: String,
        @Query("daily") daily: String,
        @Query("timezone") timezone: String,
        @Query("forecast_days") forecastDays: Int
    ): OpenMeteoForecastResponse
}

interface OpenMeteoAirApi {

    @GET("v1/air-quality")
    suspend fun current(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("timezone") timezone: String
    ): AirQualityResponse
}
