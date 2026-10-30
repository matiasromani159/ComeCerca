package com.example.proyectocomecerca.model

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

val fakeRestaurants = listOf(
    Restaurant(1, "Casa Lucía", "Española", 40.4168, -3.7038, "Calle Mayor, 5", null, 180),
    Restaurant(2, "Mar de Olivos", "Mediterránea", 40.4150, -3.6990, "Calle de Atocha, 33",
        "Mo-Su 13:00-16:00", 240),
    Restaurant(3, "Sushi Nami", "Japonesa", 40.4180, -3.7010, null, null, 410)
)