package com.example.proyectocomecerca

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectocomecerca.data.AppDatabase
import com.example.proyectocomecerca.data.Favorite
import com.example.proyectocomecerca.model.Restaurant
import com.example.proyectocomecerca.model.toRestaurant
import com.example.proyectocomecerca.network.Api
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FoodViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).favoriteDao()

    val favorites = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var restaurants by mutableStateOf<List<Restaurant>>(emptyList()); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var filter by mutableStateOf("Todas")
    var hideBad by mutableStateOf(false)

    val visible: List<Restaurant>
        get() = if (filter == "Todas") restaurants
        else restaurants.filter { it.cuisine == filter }

    fun markLoading() { loading = true; error = null }
    fun showError(msg: String) { loading = false; error = msg }

    fun search(lat: Double, lon: Double) = viewModelScope.launch {
        loading = true; error = null
        val query = "[out:json][timeout:25];" +
                "nwr[\"amenity\"=\"restaurant\"](around:1000,$lat,$lon);out center;"
        var failed = true
        // Probamos los servidores en orden; con el primero que responda, paramos
        for (url in Api.ENDPOINTS) {
            try {
                restaurants = Api.overpass.search(url, query).elements
                    .mapNotNull { it.toRestaurant(lat, lon) }
                    .sortedBy { it.distanceM }
                failed = false
                break
            } catch (e: Exception) {
                Log.e("ComeCerca", "Fallo Overpass en $url", e)
            }
        }
        if (failed) error = "No se pudieron cargar los restaurantes. Revisa tu conexión."
        else if (restaurants.isEmpty()) error = "No hay restaurantes a menos de 1 km."
        loading = false
    }

    fun restaurant(id: Long) = restaurants.find { it.id == id }
    fun favorite(id: Long) = dao.getById(id)

    fun addFavorite(r: Restaurant) = viewModelScope.launch {
        dao.insert(Favorite(r.id, r.name, r.cuisine, r.lat, r.lon, r.address))
    }
    fun update(f: Favorite) = viewModelScope.launch { dao.update(f) }
    fun delete(f: Favorite) = viewModelScope.launch { dao.delete(f) }
}