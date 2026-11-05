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
    // READ: devuelve un flow que vuelve a enviar la lista cada vez que la tabla
    //cambia , por eso favoritos se actualiza sola.
    @Query("SELECT * FROM favorites ORDER BY rating DESC, name")
    fun getAll(): Flow<List<Favorite>>

    //READ (uno)
    @Query("SELECT * FROM favorites WHERE id = :id")
    fun getById(id: Long): Flow<Favorite?>

    //CREATE. suspend = tarda, así que se ejecuta en segundo plano.
    //REPLACE: si ya existía ese id, lo sustituye en vez de fallar.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fav: Favorite)

    //UPDATE
    @Update
    suspend fun update(fav: Favorite)

    //DELETE
    @Delete
    suspend fun delete(fav: Favorite)
}