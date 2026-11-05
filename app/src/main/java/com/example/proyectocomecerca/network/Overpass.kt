package com.example.proyectocomecerca.network

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

//Forma del JSON que devuelve Overpass (solo los campos que usamos)
data class OverpassResponse(val elements: List<OsmElement>)

data class OsmElement(
    val type: String,               //"node"(un punto) o "way"(un edificio)
    val id: Long,
    val lat: Double?,               //los node traen lat/lon
    val lon: Double?,
    val center: OsmCenter?,         //los way traen el centro del edificio
    val tags: Map<String, String>?  //datos: name, cuisine, addr:street...
)

data class OsmCenter(val lat: Double, val lon: Double)

//Retrofit: una interfaz con la llamada.
//La URL completa se pasa con @Url para poder probar varios servidores.
//@Query("data") añade ?data=<consulta> a la URL.
interface OverpassApi {
    @GET
    suspend fun search(@Url url: String, @Query("data") query: String): OverpassResponse
}

//object = una unica instancia para toda la app
object Api {
    //Servidores publicos de Overpass. Se les pregunta a todos a la vez
    //y nos quedamos con el primero que responda (ver firstSuccess abajo).
    val ENDPOINTS = listOf(
        "https://maps.mail.ru/osm/tools/overpass/api/interpreter",
        "https://overpass.private.coffee/api/interpreter",
        "https://overpass-api.de/api/interpreter",
        "https://overpass.kumi.systems/api/interpreter"
    )

    //by lazy = se crea la primera vez que se usa, no al abrir la app
    val overpass: OverpassApi by lazy {
        val client = OkHttpClient.Builder()     //hacemos la conexión
            .connectTimeout(10, TimeUnit.SECONDS)   //max para conectar
            .readTimeout(30, TimeUnit.SECONDS)      //max para esperar respuesta (la consulta pide timeout:25)
            .addInterceptor(Interceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "ComeCerca-DAM/1.0").build()
                )
            }).build()
        Retrofit.Builder()
            .baseUrl("https://overpass-api.de/") //obligatoria aunque usemos @Url
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) //JSON -> clases Kotlin
            .build()
            .create(OverpassApi::class.java)        //retrofit crea la implementacion
    }
}

//Lanza la llamada a TODOS los servidores a la vez (una corrutina por servidor)
//y devuelve la respuesta del primero que conteste. Si fallan todos, devuelve null.
suspend fun <T> firstSuccess(urls: List<String>, call: suspend (String) -> T): T? = coroutineScope {
    val result = CompletableDeferred<T?>()      //"caja" donde se guarda el ganador
    val jobs = urls.map { url ->
        launch {
            try {
                result.complete(call(url))      //el primero que llega gana; los demas se ignoran
            } catch (e: CancellationException) {
                throw e                         //lo cancelamos nosotros porque otro ya gano: normal
            } catch (e: Exception) {
                if (!result.isCompleted) Log.e("ComeCerca", "Fallo Overpass en $url", e)
            }
        }
    }
    launch { jobs.joinAll(); result.complete(null) }   //si terminan todos sin exito -> null
    val r = result.await()                      //espera al primer resultado
    coroutineContext.cancelChildren()           //corta las peticiones que siguen esperando
    r
}