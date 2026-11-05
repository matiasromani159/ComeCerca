package com.example.proyectocomecerca.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.proyectocomecerca.FoodViewModel
import com.example.proyectocomecerca.data.Favorite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(vm: FoodViewModel, id: Long, onBack: () -> Unit) {
    val context = LocalContext.current                              //necesario para abrir otras apps
    val favFlow = remember(id) { vm.favorite(id) }
    val fav by favFlow.collectAsState(initial = null)
    val r = vm.restaurant(id)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val f = fav
            val name = f?.name ?: r?.name
            val lat = f?.lat ?: r?.lat
            val lon = f?.lon ?: r?.lon
            if (name == null || lat == null || lon == null) { Text("Cargando…"); return@Column }

            Text(name, style = MaterialTheme.typography.headlineMedium)
            Text(f?.cuisine ?: r?.cuisine ?: "")
            (f?.address ?: r?.address)?.let { Text(it) }
            r?.let { Text("A ${it.distanceM} m de ti") }
            r?.openingHours?.let { Text("Horario: $it") }

            Button(onClick = { openRoute(context, lat, lon) },              //abre google maps
                modifier = Modifier.fillMaxWidth()) { Text("Cómo llegar") }

            OutlinedButton(onClick = {                                    //NUEVO: Compartir
                val text = "Mira este sitio: $name\n" +
                        "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(send, "Compartir restaurante"))
            }, modifier = Modifier.fillMaxWidth()) { Text("Compartir") }

            if (f == null) {
                //No es favorito -> botón para CREAR
                OutlinedButton(
                    onClick = { r?.let { vm.addFavorite(it) } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Guardar en favoritos") }
            } else {
                //Ya es favorito -> editor para ACTUALIZAR o BORRAR
                RatingEditor(
                    fav = f,
                    onSave = {
                        vm.update(it)
                        Toast.makeText(context, "Valoración guardada",
                            Toast.LENGTH_SHORT).show()
                    },
                    onDelete = { vm.delete(f); onBack() } //borra y vuelve atrás
                )
            }
        }
    }
}

//Composable reutilizable. Recibe el favorito y DOS funciones (qué hacer al guardar/borrar)
@Composable
fun RatingEditor(fav: Favorite, onSave: (Favorite) -> Unit, onDelete: () -> Unit) {

    //Copias locales: lo que el usuario toca ANTES de pulsar "Guardar"
    var rating by remember(fav.id) { mutableIntStateOf(fav.rating) }
    var wouldReturn by remember(fav.id) { mutableStateOf(fav.wouldReturn) }
    var note by remember(fav.id) { mutableStateOf(fav.note) }

    HorizontalDivider()
    Text("Tu valoración", style = MaterialTheme.typography.titleMedium)
    Row {
        for (i in 1..5) {
            IconButton(onClick = { rating = i }) {
                Icon(Icons.Default.Star, contentDescription = "$i estrellas",
                    tint = if (i <= rating) Color(0xFFB07800) else Color(0xFFCFC6BB))
            }
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("¿Volverías?", Modifier.weight(1f))
        Switch(checked = wouldReturn, onCheckedChange = { wouldReturn = it })
    }
    OutlinedTextField(note, { note = it }, label = { Text("Nota") },
        modifier = Modifier.fillMaxWidth())
    Button(
        onClick = { onSave(fav.copy(rating = rating, wouldReturn = wouldReturn, note = note)) },
        modifier = Modifier.fillMaxWidth()
    ) { Text("Guardar valoración") }
    OutlinedButton(
        onClick = onDelete,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error)
    ) { Text("Quitar de favoritos") }
}

// Abre Google Maps (o el navegador) con la ruta andando hasta el sitio
fun openRoute(context: Context, lat: Double, lon: Double) {
    val url = "https://www.google.com/maps/dir/?api=1" +
            "&destination=$lat,$lon&travelmode=walking"
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}