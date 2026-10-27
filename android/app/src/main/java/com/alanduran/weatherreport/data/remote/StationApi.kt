package com.alanduran.weatherreport.data.remote

import kotlinx.serialization.Serializable
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface StationApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginBody): SesionDto

    @GET("dashboard")
    suspend fun dashboard(): DashboardDto
}

@Serializable
data class MensajeDto(
    val mensaje: String? = null
)

/** Mantiene las cookies de sesion (JSESSIONID) del backend WR_web. */
class SessionCookieJar : CookieJar {
    private val store = mutableMapOf<String, List<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        store[url.host] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> = store[url.host].orEmpty()
}
