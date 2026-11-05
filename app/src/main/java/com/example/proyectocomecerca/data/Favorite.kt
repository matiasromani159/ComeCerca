package com.example.proyectocomecerca.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity = esta clase es una TABLA de "sqlite" llamada "favorites". Cada propiedad es una columna.
@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val id: Long,          //el id del restaurante
    val name: String,
    val cuisine: String,
    val lat: Double,                    //coordenadas
    val lon: Double,
    val address: String?,
    val rating: Int = 0,               //0-5 estrellas, las pone el usuario
    val wouldReturn: Boolean = true,   //¿volverías?
    val note: String = ""
)