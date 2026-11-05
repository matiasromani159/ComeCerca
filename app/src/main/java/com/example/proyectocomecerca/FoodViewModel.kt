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
import com.example.proyectocomecerca.model.toRestaurant
import com.example.proyectocomecerca.network.Api
import com.example.proyectocomecerca.network.firstSuccess
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

//AndroidViewModel: guarda los datos de las pantallas y SOBREVIVE al girar el móvil
//Las pantallas no hablan con Room ni con la API: siempre pasan por aquí
class FoodViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).favoriteDao()

    //Favoritos: el Flow de Room convertido en StateFlow (guarda el último valor)
    //WhileSubscribed(5000): deja de escuchar 5 s después de que nadie lo mire
    val favorites = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    //Estado de Compose: si cambia, las pantallas que lo leen se redibujan
    //private set = solo el ViewModel puede cambiarlo
    var restaurants by mutableStateOf<List<Restaurant>>(emptyList()); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    var filter by mutableStateOf("Todas")
    var hideBad by mutableStateOf(false)

    //lo que se ve según el filtro
    val visible: List<Restaurant>
        get() = if (filter == "Todas") restaurants
        else restaurants.filter { it.cuisine == filter }

    fun markLoading() { loading = true; error = null }
    fun showError(msg: String) { loading = false; error = msg }

    fun search(lat: Double, lon: Double) = viewModelScope.launch {
        loading = true; error = null
        val query = "[out:json][timeout:25];" +
                "nwr[\"amenity\"=\"restaurant\"](around:1000,$lat,$lon);out center 60;"

        //Preguntamos a todos los servidores a la vez y nos quedamos con el primero que responda
        //Si fallan todos, result es null
        val result = firstSuccess(Api.ENDPOINTS) { url ->
            Api.overpass.search(url, query).elements
                .mapNotNull { it.toRestaurant(lat, lon) }   //JSON -> Restaurant (descarta los que no tienen nombre/posición)
                .sortedBy { it.distanceM }                  //el más cercano primero
        }

        if (result != null) restaurants = result
        //si falla y ya teníamos una lista, la dejamos en pantalla en vez de borrarla
        if (result == null && restaurants.isEmpty()) error = "No se pudieron cargar los restaurantes. Revisa tu conexión."
        else if (result != null && restaurants.isEmpty()) error = "No hay restaurantes a menos de 1 km."
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