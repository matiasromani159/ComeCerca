package com.example.proyectocomecerca.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Favorite::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // Una sola base de datos para toda la app (patrón singleton)
        //Singleton: crea la base de datos solo UNA vez para toda la app.
        //synchronized evita que dos hilos la creen a la vez.
        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java, "comecerca.db"
            ).build().also { INSTANCE = it }
        }
    }
}