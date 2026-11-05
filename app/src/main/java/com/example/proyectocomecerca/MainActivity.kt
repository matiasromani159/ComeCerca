package com.example.proyectocomecerca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectocomecerca.ui.screens.DetailScreen
import com.example.proyectocomecerca.ui.screens.FavoritesScreen
import com.example.proyectocomecerca.ui.screens.NearbyScreen
import com.example.proyectocomecerca.ui.theme.ProyectoComeCercaTheme

class MainActivity : ComponentActivity() {
    private val vm: FoodViewModel by viewModels()   //el ViewModel se crea aquí

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()                                                  //la app ocupa toda la pantalla
        setContent { ProyectoComeCercaTheme { AppNav(vm) } }
    }
}

//El "mapa" de pantallas: cada composable("ruta") es una pantalla, como las URLs de una web
@Composable
fun AppNav(vm: FoodViewModel) {
    val nav = rememberNavController()                                       //maneja historial pantallas
    NavHost(navController = nav, startDestination = "nearby") {             //empieza en Cerca
        composable("nearby") { NearbyScreen(vm, nav) }
        composable("favorites") { FavoritesScreen(vm, nav) }
        composable(
            route = "detail/{id}",   //{id} = hueco para el número
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            DetailScreen(
                vm,
                entry.arguments!!.getLong("id"),                //lee el id de la ruta
                onBack = { nav.popBackStack() }                             //volver = quitar del historial
            )
        }
    }
}

// Barra inferior compartida por Cerca y Favoritos
@Composable
fun BottomBar(nav: NavController, current: String) {
    NavigationBar {
        NavigationBarItem(
            selected = current == "nearby",
            onClick = { nav.popBackStack("nearby", inclusive = false) },        //vuelve a Cerca
            icon = { Icon(Icons.Default.Place, contentDescription = null) },
            label = { Text("Cerca") }
        )
        NavigationBarItem(
            selected = current == "favorites",
            onClick = { nav.navigate("favorites") { launchSingleTop = true } },     //no duplicarla
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text("Favoritos") }
        )
    }
}