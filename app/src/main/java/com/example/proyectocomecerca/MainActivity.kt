package com.example.proyectocomecerca

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectocomecerca.ui.screens.DetailScreen
import com.example.proyectocomecerca.ui.screens.FavoritesScreen
import com.example.proyectocomecerca.ui.screens.NearbyScreen
import com.example.proyectocomecerca.ui.theme.Coral
import com.example.proyectocomecerca.ui.theme.ProyectoComeCercaTheme
import com.example.proyectocomecerca.ui.theme.Teal
import com.example.proyectocomecerca.ui.theme.TealLight

class MainActivity : ComponentActivity() {
    private val vm: FoodViewModel by viewModels()   //el ViewModel se crea aquí

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //la app ocupa toda la pantalla; dark = iconos de hora/batería en BLANCO (van sobre la cabecera verde)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
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

//Barra inferior compartida por Cerca y Favoritos: verde, esquinas de arriba redondeadas
//y una "píldora" coral detrás del icono de la pantalla en la que estás
@Composable
fun BottomBar(nav: NavController, current: String) {
    val colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White, selectedTextColor = Color.White,
        unselectedIconColor = TealLight, unselectedTextColor = TealLight,
        indicatorColor = Coral
    )
    NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        containerColor = Teal
    ) {
        NavigationBarItem(
            selected = current == "nearby",
            onClick = { nav.popBackStack("nearby", inclusive = false) },        //vuelve a Cerca
            icon = { Icon(Icons.Default.Place, contentDescription = null) },
            label = { Text("Cerca") },
            colors = colors
        )
        NavigationBarItem(
            selected = current == "favorites",
            onClick = { nav.navigate("favorites") { launchSingleTop = true } },     //no duplicarla
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text("Favoritos") },
            colors = colors
        )
    }
}