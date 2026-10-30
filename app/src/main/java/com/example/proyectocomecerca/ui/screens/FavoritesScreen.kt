package com.example.proyectocomecerca.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.proyectocomecerca.BottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(nav: NavController) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis favoritos") }) },
        bottomBar = { BottomBar(nav, "favorites") }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aquí irán tus favoritos (día 2)")
        }
    }
}