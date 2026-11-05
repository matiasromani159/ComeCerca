package com.example.proyectocomecerca.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectocomecerca.BottomBar
import com.example.proyectocomecerca.FoodViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(vm: FoodViewModel, nav: NavController) {
    //collectAsState: escucha el StateFlow. Cuando Room cambia, esta pantalla se redibuja.
    val favorites by vm.favorites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis favoritos") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = { BottomBar(nav, "favorites") }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(
                Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no tienes favoritos. Guarda alguno desde Cerca.")
            }
        } else {
            LazyColumn(
                Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(favorites, key = { it.id }) { f ->
                    Card(
                        onClick = { nav.navigate("detail/${f.id}") },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(f.name, style = MaterialTheme.typography.titleMedium)
                            Text(f.cuisine)
                            Text(
                                "★".repeat(f.rating) + "☆".repeat(5 - f.rating) + " " +
                                        (if (f.wouldReturn) "Volvería" else "No volvería")
                            )
                            if (f.note.isNotBlank()) {
                                Text(f.note, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}