package com.example.proyectocomecerca.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyectocomecerca.BottomBar
import com.example.proyectocomecerca.FoodViewModel
import com.example.proyectocomecerca.data.Favorite
import com.example.proyectocomecerca.ui.theme.Coral
import com.example.proyectocomecerca.ui.theme.Ink
import com.example.proyectocomecerca.ui.theme.InkSoft
import com.example.proyectocomecerca.ui.theme.Mist
import com.example.proyectocomecerca.ui.theme.Page

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(vm: FoodViewModel, nav: NavController) {
    //collectAsState: escucha el StateFlow. Cuando Room cambia, esta pantalla se redibuja.
    val favorites by vm.favorites.collectAsState()

    Scaffold(
        containerColor = Page,
        contentWindowInsets = WindowInsets(0),      //la cabecera verde sube hasta arriba del todo
        bottomBar = { BottomBar(nav, "favorites") }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 16.dp)
        ) {
            item {
                Header {
                    Text("Mis favoritos", Modifier.padding(top = 14.dp),
                        style = MaterialTheme.typography.headlineMedium)
                    //resumen: cuántos hay guardados y a cuántos volverías
                    Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pill("${favorites.size} guardados", Color.White.copy(alpha = 0.18f), Color.White)
                        Pill("${favorites.count { it.wouldReturn }} volverías", Coral, Color.White)
                    }
                }
            }
            item { Spacer(Modifier.height(10.dp)) }

            if (favorites.isEmpty()) {
                item {
                    Text("Aún no tienes favoritos.\nGuarda alguno desde Cerca.",
                        Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center, color = InkSoft)
                }
            } else {
                items(favorites, key = { it.id }) { f ->
                    FavoriteRow(
                        f,
                        onClick = { nav.navigate("detail/${f.id}") },
                        //animateItem: si cambias la nota o lo borras, la lista se recoloca animada
                        modifier = Modifier.animateItem()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

//Fila de favorito: foto, nombre, tu nota, estrellas y "Volvería / No volvería"
@Composable
fun FavoriteRow(f: Favorite, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FoodPhoto(f.cuisine, 78.dp, RoundedCornerShape(20.dp))
            Column(
                Modifier.weight(1f).padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(f.name, style = MaterialTheme.typography.titleMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                //si no hay nota, enseñamos la cocina
                Text(f.note.ifBlank { f.cuisine },
                    style = MaterialTheme.typography.bodySmall, color = InkSoft,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) { Stars(f.rating) }
                    if (f.wouldReturn) Pill("Volvería", Coral, Color.White)
                    else Pill("No volvería", Mist, Ink)
                }
            }
        }
    }
}