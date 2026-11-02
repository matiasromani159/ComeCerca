package com.example.proyectocomecerca.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// @Dao = las consultas. Aquí está el CRUD completo.
@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY rating DESC, name")
    fun getAll(): Flow<List<Favorite>>          // READ: avisa solo cuando cambia algo

    @Query("SELECT * FROM favorites WHERE id = :id")
    fun getById(id: Long): Flow<Favorite?>      // READ (uno)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fav: Favorite)           // CREATE

    @Update
    suspend fun update(fav: Favorite)           // UPDATE

    @Delete
    suspend fun delete(fav: Favorite)           // DELETE
}