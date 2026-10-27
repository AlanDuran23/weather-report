package com.alanduran.weatherreport.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginBody(
    val usuario: String,
    val contrasena: String
)

@Serializable
data class SesionDto(
    val id: Int? = null,
    val nombreCompleto: String? = null,
    val usuario: String? = null,
    val rol: String? = null
)

@Serializable
data class DashboardDto(
    val ultimo: RegistroDto? = null,
    @SerialName("promHumedad24") val promHumedad24: Double = 0.0,
    @SerialName("promViento24") val promViento24: Double = 0.0,
    @SerialName("promTemp24") val promTemp24: Double = 0.0,
    @SerialName("hayDatos24") val hayDatos24: Boolean = false,
    @SerialName("nombreEstacion") val nombreEstacion: String? = null,
    val ubicacion: String? = null
)

@Serializable
data class RegistroDto(
    val id: Int? = null,
    val fecha: String? = null,
    val hora: String? = null,
    @SerialName("tempOut") val tempOut: Double? = null,
    @SerialName("hiTemp") val hiTemp: Double? = null,
    @SerialName("lowTemp") val lowTemp: Double? = null,
    @SerialName("dewPt") val dewPt: Double? = null,
    @SerialName("windChill") val windChill: Double? = null,
    @SerialName("heatIndex") val heatIndex: Double? = null,
    @SerialName("outHum") val outHum: Double? = null,
    @SerialName("windSpeed") val windSpeed: Double? = null,
    @SerialName("windDir") val windDir: String? = null,
    val bar: Double? = null,
    val rain: Double? = null,
    val observaciones: String? = null
)
