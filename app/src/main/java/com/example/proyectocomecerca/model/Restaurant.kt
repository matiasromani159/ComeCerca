package com.example.proyectocomecerca.model

import android.location.Location
import com.example.proyectocomecerca.network.OsmElement

// El "molde" de un restaurante. El ? significa "puede venir vacío (null)".
data class Restaurant(
    val id: Long,
    val name: String,
    val cuisine: String,
    val lat: Double,
    val lon: Double,
    val address: String?,
    val openingHours: String?,
    val distanceM: Int
)

// Datos de mentira. El día 5 se borran (cuando search() esté en el ViewModel).
val fakeRestaurants = listOf(
    Restaurant(1, "Casa Lucía", "Española", 40.4168, -3.7038, "Calle Mayor, 5", null, 180),
    Restaurant(2, "Mar de Olivos", "Mediterránea", 40.4150, -3.6990, "Calle de Atocha, 33",
        "Mo-Su 13:00-16:00", 240),
    Restaurant(3, "Sushi Nami", "Japonesa", 40.4180, -3.7010, null, null, 410)
)

// Convierte un elemento de OpenStreetMap en nuestro Restaurant (o null si no sirve)
fun OsmElement.toRestaurant(myLat: Double, myLon: Double): Restaurant? {
    val t = tags ?: return null
    val name = t["name"] ?: return null            // sin nombre no nos sirve
    val la = lat ?: center?.lat ?: return null
    val lo = lon ?: center?.lon ?: return null

    val dist = FloatArray(1)
    Location.distanceBetween(myLat, myLon, la, lo, dist) // metros hasta ti

    val street = t["addr:street"]
    val number = t["addr:housenumber"]
    val address = when {
        street == null -> null
        number == null -> street
        else -> "$street, $number"
    }
    val uid = if (type == "node") id else -id      // evita ids repetidos
    return Restaurant(uid, name, cuisineLabel(t["cuisine"]), la, lo,
        address, t["opening_hours"], dist[0].toInt())
}

// OpenStreetMap guarda la cocina en inglés: la traducimos a nuestras categorías
fun cuisineLabel(raw: String?): String =
    when (raw?.split(";")?.firstOrNull()?.trim()?.lowercase()) {
        null, "" -> "Sin especificar"
        "mediterranean" -> "Mediterránea"
        "sushi", "japanese" -> "Japonesa"
        "pizza", "italian" -> "Italiana"
        "spanish", "regional", "tapas" -> "Española"
        "burger", "american" -> "Hamburguesas"
        "chinese" -> "China"
        "mexican" -> "Mexicana"
        else -> "Otras"
    }

val FILTERS = listOf("Todas", "Mediterránea", "Japonesa", "Italiana", "Española",
    "Hamburguesas", "China", "Mexicana", "Otras", "Sin especificar")