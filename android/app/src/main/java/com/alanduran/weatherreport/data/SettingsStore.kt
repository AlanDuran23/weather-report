package com.alanduran.weatherreport.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.weatherDataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_settings")

data class AppSettings(
    val backendHost: String = "10.0.2.2:8080",
    val useStation: Boolean = false,
    val latitude: Double = 19.4326,
    val longitude: Double = -99.1332,
    val locationName: String = "Ciudad de México",
    val stationUser: String = "",
    val stationPassword: String = "",
    val refreshMinutes: Int = 15
)

class SettingsStore(private val context: Context) {

    private object Keys {
        val BACKEND_HOST = stringPreferencesKey("backend_host")
        val USE_STATION = booleanPreferencesKey("use_station")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val LOCATION_NAME = stringPreferencesKey("location_name")
        val STATION_USER = stringPreferencesKey("station_user")
        val STATION_PASSWORD = stringPreferencesKey("station_password")
        val REFRESH_MINUTES = intPreferencesKey("refresh_minutes")
    }

    val data: Flow<AppSettings> = context.weatherDataStore.data.map { prefs ->
        val defaults = AppSettings()
        AppSettings(
            backendHost = prefs[Keys.BACKEND_HOST] ?: defaults.backendHost,
            useStation = prefs[Keys.USE_STATION] ?: defaults.useStation,
            latitude = prefs[Keys.LATITUDE] ?: defaults.latitude,
            longitude = prefs[Keys.LONGITUDE] ?: defaults.longitude,
            locationName = prefs[Keys.LOCATION_NAME] ?: defaults.locationName,
            stationUser = prefs[Keys.STATION_USER] ?: defaults.stationUser,
            stationPassword = prefs[Keys.STATION_PASSWORD] ?: defaults.stationPassword,
            refreshMinutes = prefs[Keys.REFRESH_MINUTES] ?: defaults.refreshMinutes
        )
    }

    suspend fun update(settings: AppSettings) {
        context.weatherDataStore.edit { prefs ->
            prefs[Keys.BACKEND_HOST] = settings.backendHost
            prefs[Keys.USE_STATION] = settings.useStation
            prefs[Keys.LATITUDE] = settings.latitude
            prefs[Keys.LONGITUDE] = settings.longitude
            prefs[Keys.LOCATION_NAME] = settings.locationName
            prefs[Keys.STATION_USER] = settings.stationUser
            prefs[Keys.STATION_PASSWORD] = settings.stationPassword
            prefs[Keys.REFRESH_MINUTES] = settings.refreshMinutes
        }
    }
}
