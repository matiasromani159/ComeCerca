package com.example.proyectocomecerca.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Forma del JSON que devuelve Overpass (solo los campos que usamos)
data class OverpassResponse(val elements: List<OsmElement>)

data class OsmElement(
    val type: String,               // "node" o "way"
    val id: Long,
    val lat: Double?,               // los node traen lat/lon
    val lon: Double?,
    val center: OsmCenter?,         // los way (edificios) traen center
    val tags: Map<String, String>?  // name, cuisine, addr:street...
)

data class OsmCenter(val lat: Double, val lon: Double)

// Retrofit: una interfaz con la llamada. Retrofit escribe el código por ti.
interface OverpassApi {
    @GET("api/interpreter")
    suspend fun search(@Query("data") query: String): OverpassResponse
}

object Api {
    val overpass: OverpassApi by lazy {
        val client = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS) // Overpass a veces tarda
            .addInterceptor(Interceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "ComeCerca-DAM/1.0").build()
                )
            }).build()
        Retrofit.Builder()
            .baseUrl("https://overpass-api.de/") // HTTPS
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) // JSON -> clases Kotlin
            .build()
            .create(OverpassApi::class.java)
    }
}