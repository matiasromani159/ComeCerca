package com.example.proyectocomecerca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.proyectocomecerca.ui.screens.FavoritesScreen
import com.example.proyectocomecerca.ui.screens.NearbyScreen
import com.example.proyectocomecerca.ui.screens.DetailScreen
import com.example.proyectocomecerca.ui.theme.ProyectoComeCercaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ProyectoComeCercaTheme { AppNav() } }
    }
}

// El "mapa" de pantallas: cada composable("ruta") es una pantalla.
@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "nearby") {
        composable("nearby") { NearbyScreen(nav) }
        composable("favorites") { FavoritesScreen(nav) }
        composable(
            route = "detail/{id}",   // {id} = hueco para el número
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            DetailScreen(entry.arguments!!.getLong("id"), onBack = { nav.popBackStack() })
        }
    }
}

// Barra inferior compartida por Cerca y Favoritos
@Composable
fun BottomBar(nav: NavController, current: String) {
    NavigationBar {
        NavigationBarItem(
            selected = current == "nearby",
            onClick = { nav.popBackStack("nearby", inclusive = false) },
            icon = { Icon(Icons.Default.Place, contentDescription = null) },
            label = { Text("Cerca") }
        )
        NavigationBarItem(
            selected = current == "favorites",
            onClick = { nav.navigate("favorites") { launchSingleTop = true } },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text("Favoritos") }
        )
    }
}