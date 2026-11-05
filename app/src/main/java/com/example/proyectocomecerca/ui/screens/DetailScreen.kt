package com.example.proyectocomecerca.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.proyectocomecerca.FoodViewModel
import com.example.proyectocomecerca.data.Favorite
import com.example.proyectocomecerca.ui.theme.Coral
import com.example.proyectocomecerca.ui.theme.Gold
import com.example.proyectocomecerca.ui.theme.GreyStar
import com.example.proyectocomecerca.ui.theme.Ink
import com.example.proyectocomecerca.ui.theme.InkSoft
import com.example.proyectocomecerca.ui.theme.Mist
import com.example.proyectocomecerca.ui.theme.Page
import com.example.proyectocomecerca.ui.theme.Teal
import com.example.proyectocomecerca.ui.theme.TealSoft
import com.example.proyectocomecerca.ui.theme.Tile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(vm: FoodViewModel, id: Long, onBack: () -> Unit) {
    val context = LocalContext.current                              //necesario para abrir otras apps
    val favFlow = remember(id) { vm.favorite(id) }
    val fav by favFlow.collectAsState(initial = null)
    val r = vm.restaurant(id)

    //Los datos salen del favorito (Room) o, si no lo es, del restaurante de la API
    val f = fav
    val name = f?.name ?: r?.name
    val lat = f?.lat ?: r?.lat
    val lon = f?.lon ?: r?.lon
    val cuisine = f?.cuisine ?: r?.cuisine ?: "Sin especificar"

    Scaffold(containerColor = Page, contentWindowInsets = WindowInsets(0)) { padding ->
        if (name == null || lat == null || lon == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Teal)
            }
            return@Scaffold
        }

        Column(
            Modifier.padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            //CABECERA verde + "plato" con la foto que sobresale por abajo
            Box(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth()
                        .background(Teal, RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp))
                        .statusBarsPadding()
                        .padding(start = 8.dp, top = 8.dp, bottom = 32.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                    //end = 150 dp: deja libre la zona de la derecha, donde va el plato
                    Column(
                        Modifier.padding(start = 12.dp, end = 150.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Pill(cuisine, Color.White.copy(alpha = 0.18f), Color.White)
                        Text(name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    }
                }
                //offset: lo bajamos 75 dp para que quede medio fuera de la cabecera
                Box(
                    Modifier.align(Alignment.BottomEnd)
                        .offset(x = (-20).dp, y = 75.dp)
                        .size(150.dp)
                        .shadow(10.dp, CircleShape)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    FoodPhoto(cuisine, 124.dp, CircleShape)
                }
            }

            //DATOS (a la izquierda del plato)
            Column(
                Modifier.padding(start = 20.dp, end = 180.dp, top = 16.dp).heightIn(min = 70.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                r?.let { Pill("A ${it.distanceM} m", TealSoft, Teal) }
                (f?.address ?: r?.address)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = InkSoft)
                }
                r?.openingHours?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = InkSoft)
                }
            }

            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { openRoute(context, lat, lon) },          //abre Google Maps
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Coral)
                    ) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cómo llegar")
                    }
                    OutlinedButton(
                        onClick = { share(context, name, lat, lon) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        border = BorderStroke(1.5.dp, Teal),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal)
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Compartir")
                    }
                }

                if (f == null) {
                    //No es favorito -> botón para CREAR
                    Button(
                        onClick = { r?.let { vm.addFavorite(it) } },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal)
                    ) {
                        Icon(Icons.Default.FavoriteBorder, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Guardar en favoritos")
                    }
                } else {
                    //Ya es favorito -> editor para ACTUALIZAR o BORRAR
                    RatingEditor(
                        fav = f,
                        onSave = {
                            vm.update(it)
                            Toast.makeText(context, "Valoración guardada", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { vm.delete(f); onBack() }   //borra y vuelve atrás
                    )
                }
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

    //Panel gris con todo el editor dentro
    Column(
        Modifier.fillMaxWidth()
            .background(Tile, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Tu valoración", style = MaterialTheme.typography.titleMedium, color = Teal)

        Row(Modifier.offset(x = (-8).dp)) {
            for (i in 1..5) {
                //la estrella elegida "crece" con una animación
                val scale by animateFloatAsState(if (i == rating) 1.25f else 1f, label = "estrella$i")
                IconButton(onClick = { rating = i }) {
                    Icon(
                        Icons.Default.Star, contentDescription = "$i estrellas",
                        tint = if (i <= rating) Gold else GreyStar,
                        modifier = Modifier.size(32.dp).scale(scale)
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(start = 16.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("¿Volverías?", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
            Switch(
                checked = wouldReturn,
                onCheckedChange = { wouldReturn = it },
                colors = SwitchDefaults.colors(checkedTrackColor = Teal, checkedThumbColor = Color.White)
            )
        }

        OutlinedTextField(
            value = note, onValueChange = { note = it },
            label = { Text("Nota") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedBorderColor = Teal, unfocusedBorderColor = Color.Transparent,
                focusedLabelColor = Teal
            )
        )

        //Quitar y Guardar lado a lado
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onDelete,
                modifier = Modifier.weight(1f).height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Mist, contentColor = Ink)
            ) { Text("Quitar") }
            Button(
                onClick = { onSave(fav.copy(rating = rating, wouldReturn = wouldReturn, note = note)) },
                modifier = Modifier.weight(1.4f).height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal)
            ) { Text("Guardar") }
        }
    }
}

//Abre Google Maps (o el navegador) con la ruta andando hasta el sitio.
//try/catch: si el móvil no tiene ninguna app que lo abra, avisamos en vez de cerrar la app
fun openRoute(context: Context, lat: Double, lon: Double) {
    val url = "https://www.google.com/maps/dir/?api=1" +
            "&destination=$lat,$lon&travelmode=walking"
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No hay ninguna app para abrir el mapa", Toast.LENGTH_SHORT).show()
    }
}

//Abre el menú de "Compartir" de Android con el nombre y un enlace de Google Maps
fun share(context: Context, name: String, lat: Double, lon: Double) {
    val text = "Mira este sitio: $name\n" +
            "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    try {
        context.startActivity(Intent.createChooser(send, "Compartir restaurante"))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No hay ninguna app para compartir", Toast.LENGTH_SHORT).show()
    }
}