package com.alanduran.weatherreport.data.repository

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import com.alanduran.weatherreport.data.AppSettings
import com.alanduran.weatherreport.data.SettingsStore
import com.alanduran.weatherreport.data.model.AirQuality
import com.alanduran.weatherreport.data.model.CurrentWeather
import com.alanduran.weatherreport.data.model.DailyPoint
import com.alanduran.weatherreport.data.model.HourlyPoint
import com.alanduran.weatherreport.data.model.SunTimes
import com.alanduran.weatherreport.data.model.UvInfo
import com.alanduran.weatherreport.data.model.WeatherData
import com.alanduran.weatherreport.data.model.WeatherSource
import com.alanduran.weatherreport.data.remote.AirQualityResponse
import com.alanduran.weatherreport.data.remote.DailyDto
import com.alanduran.weatherreport.data.remote.DashboardDto
import com.alanduran.weatherreport.data.remote.HourlyDto
import com.alanduran.weatherreport.data.remote.LoginBody
import com.alanduran.weatherreport.data.remote.OpenMeteoAirApi
import com.alanduran.weatherreport.data.remote.OpenMeteoForecastApi
import com.alanduran.weatherreport.data.remote.OpenMeteoForecastResponse
import com.alanduran.weatherreport.data.remote.SessionCookieJar
import com.alanduran.weatherreport.data.remote.StationApi
import com.alanduran.weatherreport.util.aqiCategory
import com.alanduran.weatherreport.util.uvCategory
import com.alanduran.weatherreport.util.weatherConditionFromCode
import com.alanduran.weatherreport.util.weatherConditionFromText
import okhttp3.CookieJar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class WeatherRepository(private val settingsStore: SettingsStore) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val forecastApi: OpenMeteoForecastApi =
        buildRetrofit("https://api.open-meteo.com/", null).create(OpenMeteoForecastApi::class.java)

    private val airApi: OpenMeteoAirApi =
        buildRetrofit("https://air-quality-api.open-meteo.com/", null).create(OpenMeteoAirApi::class.java)

    private var stationBaseUrl: String? = null
    private var stationApi: StationApi? = null

    suspend fun load(): WeatherData = coroutineScope {
        val settings = settingsStore.data.first()

        val forecastDeferred = async {
            forecastApi.forecast(
                latitude = settings.latitude,
                longitude = settings.longitude,
                current = CURRENT_FIELDS,
                hourly = HOURLY_FIELDS,
                daily = DAILY_FIELDS,
                timezone = TIMEZONE,
                forecastDays = 7
            )
        }
        val airDeferred = async {
            runCatching {
                airApi.current(
                    latitude = settings.latitude,
                    longitude = settings.longitude,
                    current = AIR_FIELDS,
                    timezone = TIMEZONE
                )
            }.getOrNull()
        }
        val stationDeferred = async {
            if (settings.useStation) {
                withTimeoutOrNull(STATION_TIMEOUT_MS) {
                    runCatching { loadStation(settings) }.getOrNull()
                }
            } else {
                null
            }
        }

        val forecast = forecastDeferred.await()
        val air = airDeferred.await()
        val station = stationDeferred.await()

        buildWeatherData(settings, forecast, air, station)
    }

    private suspend fun loadStation(settings: AppSettings): DashboardDto {
        val api = stationApi(settings.backendHost)
        // El usuario y la contraseña solo viajan sin cifrar (http) dentro de la red local.
        check(canSendCredentials(stationBaseUrl.orEmpty())) {
            "Fuera de la red local la estación debe usar https://"
        }
        runCatching { api.login(LoginBody(settings.stationUser, settings.stationPassword)) }
        return api.dashboard()
    }

    private fun buildRetrofit(baseUrl: String, cookieJar: CookieJar?): Retrofit {
        val timeoutSeconds = if (cookieJar != null) 5L else 12L
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
        if (cookieJar != null) {
            clientBuilder.cookieJar(cookieJar)
        }
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(clientBuilder.build())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    private fun stationApi(host: String): StationApi {
        val normalized = normalizeHost(host)
        val cached = stationApi
        if (cached == null || normalized != stationBaseUrl) {
            stationBaseUrl = normalized
            return buildRetrofit(normalized, SessionCookieJar())
                .create(StationApi::class.java)
                .also { stationApi = it }
        }
        return cached
    }

    /** true si la URL usa https o apunta a la red local (localhost, 10.x, 172.16-31.x, 192.168.x, .local). */
    private fun canSendCredentials(url: String): Boolean {
        val parsed = url.toHttpUrlOrNull() ?: return false
        if (parsed.isHttps) return true
        val host = parsed.host.lowercase()
        if (host == "localhost" || host.endsWith(".local")) return true
        val octets = host.split(".").map { it.toIntOrNull() ?: return false }
        if (octets.size != 4) return false
        return octets[0] == 127 || octets[0] == 10 ||
            (octets[0] == 192 && octets[1] == 168) ||
            (octets[0] == 172 && octets[1] in 16..31)
    }

    private fun normalizeHost(host: String): String {
        val trimmed = host.trim().removeSuffix("/")
        val withScheme =
            if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed
            else "http://$trimmed"
        return if (withScheme.endsWith("/api")) "$withScheme/" else "$withScheme/api/"
    }

    private fun buildWeatherData(
        settings: AppSettings,
        forecast: OpenMeteoForecastResponse,
        air: AirQualityResponse?,
        station: DashboardDto?
    ): WeatherData {
        val current = forecast.current
        val daily = forecast.daily
        val registro = station?.ultimo

        var condition = weatherConditionFromCode(current?.weatherCode)
        weatherConditionFromText(registro?.observaciones)?.let { condition = it }

        val temperature = registro?.tempOut ?: current?.temperature ?: 0.0
        val humidity = (registro?.outHum ?: current?.humidity?.toDouble() ?: 0.0).roundToInt()
        val pressure = registro?.bar ?: current?.pressureMsl ?: current?.surfacePressure ?: 0.0
        val windSpeed = registro?.windSpeed ?: current?.windSpeed ?: 0.0
        val windDirection = current?.windDirection ?: 0
        val windDirectionLabel = registro?.windDir?.takeIf { it.isNotBlank() } ?: compassLabel(windDirection)
        val feelsLike = registro?.windChill ?: current?.apparentTemperature ?: temperature
        val tempMax = registro?.hiTemp ?: daily?.tempMax?.firstOrNull() ?: temperature
        val tempMin = registro?.lowTemp ?: daily?.tempMin?.firstOrNull() ?: temperature
        val isDay = (current?.isDay ?: 1) == 1
        val aqi = air?.current?.usAqi

        val uvIndex = daily?.uvIndexMax?.firstOrNull() ?: 0.0

        val airQuality = air?.current?.let { c ->
            val category = aqiCategory(c.usAqi)
            AirQuality(
                aqi = c.usAqi,
                category = category.label,
                categoryColor = category.color,
                pm25 = c.pm25,
                pm10 = c.pm10,
                ozone = c.ozone,
                no2 = c.no2,
                so2 = c.so2,
                co = c.co
            )
        }

        return WeatherData(
            location = station?.ubicacion?.takeIf { it.isNotBlank() } ?: settings.locationName,
            stationName = station?.nombreEstacion,
            current = CurrentWeather(
                temperature = temperature,
                feelsLike = feelsLike,
                humidity = humidity,
                pressure = pressure,
                conditionLabel = condition.label,
                conditionEmoji = condition.emoji,
                isDay = isDay,
                windSpeed = windSpeed,
                windDirection = windDirection,
                windDirectionLabel = windDirectionLabel,
                tempMax = tempMax,
                tempMin = tempMin,
                aqi = aqi
            ),
            hourly = buildHourly(forecast.hourly, current?.time),
            daily = buildDaily(daily),
            airQuality = airQuality,
            uv = UvInfo(uvIndex, uvCategory(uvIndex)),
            sun = SunTimes(
                sunrise = parseDateTime(daily?.sunrise?.firstOrNull())?.toLocalTime(),
                sunset = parseDateTime(daily?.sunset?.firstOrNull())?.toLocalTime()
            ),
            source = if (registro != null) WeatherSource.MIXED else WeatherSource.OPEN_METEO,
            stationUpdatedAt = registro?.let { "${it.fecha.orEmpty()} ${it.hora.orEmpty()}".trim() },
            updatedAt = LocalTime.now()
        )
    }

    private fun buildHourly(hourly: HourlyDto?, currentTime: String?): List<HourlyPoint> {
        if (hourly == null || hourly.time.isEmpty()) return emptyList()
        val currentHour = parseDateTime(currentTime)?.truncatedTo(ChronoUnit.HOURS)
        var startIndex = 0
        if (currentHour != null) {
            val index = hourly.time.indexOfFirst { value ->
                parseDateTime(value)?.let { !it.isBefore(currentHour) } == true
            }
            if (index >= 0) startIndex = index
        }
        val end = (startIndex + 24).coerceAtMost(hourly.time.size)
        return (startIndex until end).mapNotNull { i ->
            val time = parseDateTime(hourly.time.getOrNull(i)) ?: return@mapNotNull null
            HourlyPoint(
                time = time.toLocalTime(),
                temperature = hourly.temperature.getOrNull(i) ?: 0.0,
                weatherCode = hourly.weatherCode.getOrNull(i) ?: 3,
                windSpeed = hourly.windSpeed.getOrNull(i) ?: 0.0,
                precipitationProbability = hourly.precipitation.getOrNull(i) ?: 0
            )
        }
    }

    private fun buildDaily(daily: DailyDto?): List<DailyPoint> {
        if (daily == null) return emptyList()
        return daily.time.indices.take(7).mapNotNull { i ->
            val date = parseDate(daily.time.getOrNull(i)) ?: return@mapNotNull null
            val condition = weatherConditionFromCode(daily.weatherCode.getOrNull(i))
            DailyPoint(
                date = date,
                label = dayLabel(date, i),
                conditionLabel = condition.label,
                conditionEmoji = condition.emoji,
                tempMax = daily.tempMax.getOrNull(i) ?: 0.0,
                tempMin = daily.tempMin.getOrNull(i) ?: 0.0,
                precipitationProbability = daily.precipitation.getOrNull(i) ?: 0
            )
        }
    }

    private fun dayLabel(date: LocalDate, index: Int): String = when (index) {
        0 -> "Hoy"
        1 -> "Ma\u00F1ana"
        else -> DAY_NAMES[date.dayOfWeek.value - 1].replaceFirstChar { it.uppercase() }
    }

    private fun compassLabel(degrees: Int): String {
        val index = ((((degrees % 360) + 360) % 360 + 22) / 45) % 8
        return COMPASS[index]
    }

    private fun parseDateTime(value: String?): LocalDateTime? = try {
        if (value.isNullOrBlank()) null else LocalDateTime.parse(value)
    } catch (e: Exception) {
        null
    }

    private fun parseDate(value: String?): LocalDate? = try {
        if (value.isNullOrBlank()) null else LocalDate.parse(value)
    } catch (e: Exception) {
        null
    }

    private companion object {
        const val TIMEZONE = "America/Mexico_City"
        const val STATION_TIMEOUT_MS = 6_000L
        const val CURRENT_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code," +
                "wind_speed_10m,wind_direction_10m,surface_pressure,pressure_msl"
        const val HOURLY_FIELDS =
            "temperature_2m,weather_code,wind_speed_10m,precipitation_probability"
        const val DAILY_FIELDS =
            "weather_code,temperature_2m_max,temperature_2m_min," +
                "precipitation_probability_max,uv_index_max,sunrise,sunset"
        const val AIR_FIELDS =
            "us_aqi,pm2_5,pm10,ozone,nitrogen_dioxide,sulphur_dioxide,carbon_monoxide"

        val COMPASS = listOf("N", "NE", "E", "SE", "S", "SO", "O", "NO")
        val DAY_NAMES = listOf(
            "lunes", "martes", "mi\u00E9rcoles", "jueves", "viernes", "s\u00E1bado", "domingo"
        )
    }
}
