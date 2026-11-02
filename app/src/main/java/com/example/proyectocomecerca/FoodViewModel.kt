package com.example.proyectocomecerca

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectocomecerca.data.AppDatabase
import com.example.proyectocomecerca.data.Favorite
import com.example.proyectocomecerca.model.Restaurant
import com.example.proyectocomecerca.model.fakeRestaurants
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// El ViewModel guarda los datos de las pantallas y sobrevive al girar el móvil.
class FoodViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).favoriteDao()

    // Favoritos: se actualiza solo cada vez que cambia la base de datos
    val favorites = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Restaurantes cercanos: de momento los de mentira (día 5: la API)
    var restaurants by mutableStateOf(fakeRestaurants)
        private set

    fun restaurant(id: Long) = restaurants.find { it.id == id }
    fun favorite(id: Long) = dao.getById(id)

    // launch = ejecutar en segundo plano para no congelar la pantalla
    fun addFavorite(r: Restaurant) = viewModelScope.launch {
        dao.insert(Favorite(r.id, r.name, r.cuisine, r.lat, r.lon, r.address))
    }
    fun update(f: Favorite) = viewModelScope.launch { dao.update(f) }
    fun delete(f: Favorite) = viewModelScope.launch { dao.delete(f) }
}