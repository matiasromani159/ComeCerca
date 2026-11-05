package com.example.proyectocomecerca.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.proyectocomecerca.BottomBar
import com.example.proyectocomecerca.FoodViewModel
import com.example.proyectocomecerca.data.Favorite
import com.example.proyectocomecerca.getLocation
import com.example.proyectocomecerca.model.FILTERS
import com.example.proyectocomecerca.model.Restaurant
import com.example.proyectocomecerca.ui.theme.Coral
import com.example.proyectocomecerca.ui.theme.InkSoft
import com.example.proyectocomecerca.ui.theme.Mint
import com.example.proyectocomecerca.ui.theme.Page
import com.example.proyectocomecerca.ui.theme.Teal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(vm: FoodViewModel, nav: NavController) {
    val context = LocalContext.current
    val favorites by vm.favorites.collectAsState()
    val favById = favorites.associateBy { it.id }           //id -> Favorite (corazón y estrellas)

    //Lista final: filtro de cocina + ocultar donde no volverías
    val list = vm.visible.filterNot { vm.hideBad && favById[it.id]?.wouldReturn == false }

    //GPS -> API
    fun searchHere() {
        vm.markLoading()
        getLocation(context,
            onResult = { lat, lon -> vm.search(lat, lon) },
            onFail = { vm.showError("No se pudo obtener tu ubicación. Activa el GPS.") })
    }

    //PERMISO (requisito): este "lanzador" muestra el diálogo de Android
    //Lo de dentro se ejecuta cuando el usuario responde
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) searchHere()
        else vm.showError("Sin permiso de ubicación no puedo buscar restaurantes cerca de ti.")
    }

    //¿Ya tengo permiso? Sí -> busco. No -> lo pido
    fun start() {
        val granted = listOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(context, it) ==
                    PackageManager.PERMISSION_GRANTED }
        if (granted) searchHere()
        else launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    //Se ejecuta UNA vez al entrar en la pantalla (no en cada redibujado)
    //Si ya hay restaurantes (volvemos de Detalle), no repite la búsqueda
    LaunchedEffect(Unit) { if (vm.restaurants.isEmpty()) start() }

    //Texto pequeño de la cabecera
    val subtitle = when {
        vm.loading -> "Buscando sitios cerca de ti…"
        vm.restaurants.isNotEmpty() -> "${vm.restaurants.size} sitios a menos de 1 km de ti"
        else -> "Restaurantes a menos de 1 km"
    }

    Scaffold(
        containerColor = Page,
        contentWindowInsets = WindowInsets(0),      //la cabecera verde sube hasta arriba del todo
        bottomBar = { BottomBar(nav, "nearby") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    list.filter { favById[it.id]?.wouldReturn != false }  //sin los "no volvería"
                        .randomOrNull()
                        ?.let { nav.navigate("detail/${it.id}") }
                },
                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                text = { Text("Sorpréndeme") },
                containerColor = Coral,
                contentColor = Color.White,
                shape = RoundedCornerShape(26.dp)
            )
        }
    ) { padding ->
        val err = vm.error
        //Toda la pantalla es UNA lista: así la cabecera y las categorías suben al hacer scroll
        LazyColumn(
            Modifier.fillMaxSize(),
            //abajo: alto de la barra inferior + 88 dp para que el botón flotante no tape la última fila
            contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 88.dp)
        ) {
            item {
                Header {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        //"logo": círculo blanco con la chincheta
                        Box(
                            Modifier.size(36.dp).background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Place, null, tint = Teal, modifier = Modifier.size(20.dp))
                        }
                        Text("ComeCerca", Modifier.padding(start = 10.dp).weight(1f),
                            style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { start() }) {
                            Icon(Icons.Default.Refresh, "Buscar de nuevo")
                        }
                    }
                    Text("¿Dónde comemos hoy?", Modifier.padding(top = 14.dp),
                        style = MaterialTheme.typography.headlineMedium)
                    Text(subtitle, color = Mint, style = MaterialTheme.typography.bodyMedium)
                }
            }

            item { SectionTitle("Categorías") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(FILTERS) { label ->
                        CategoryTile(label, selected = vm.filter == label,
                            onClick = { vm.filter = label })
                    }
                }
            }

            item {
                Row(
                    Modifier.fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cerca de ti", Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium, color = Teal)
                    FilterChip(
                        selected = vm.hideBad,
                        onClick = { vm.hideBad = !vm.hideBad },
                        label = { Text("Ocultar donde no volvería",
                            style = MaterialTheme.typography.labelMedium) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Teal,
                            selectedContainerColor = Teal,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = vm.hideBad,
                            borderColor = Teal, selectedBorderColor = Teal
                        )
                    )
                }
            }

            when {
                vm.loading -> item {
                    Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Teal)
                    }
                }
                err != null -> item { ErrorBox(err, onRetry = { start() }) }
                list.isEmpty() -> item {
                    Text("No hay restaurantes de este tipo cerca",
                        Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center, color = InkSoft)
                }
                else -> items(list, key = { it.id }) { r ->
                    RestaurantRow(
                        r, favById[r.id],
                        onClick = { nav.navigate("detail/${r.id}") },
                        //animateItem: al filtrar, las filas se recolocan con animación
                        modifier = Modifier.animateItem()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

//Una fila de la lista: foto de la cocina, nombre, tu valoración (si es favorito) y distancia
@Composable
fun RestaurantRow(r: Restaurant, fav: Favorite?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            FoodPhoto(r.cuisine, 66.dp, RoundedCornerShape(18.dp))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(r.name, style = MaterialTheme.typography.titleMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(r.cuisine, style = MaterialTheme.typography.bodySmall, color = InkSoft)
                if (fav != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Stars(fav.rating, 12.sp)
                        Text(if (fav.wouldReturn) " · Volverías" else " · No volverías",
                            style = MaterialTheme.typography.bodySmall, color = InkSoft)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (fav != null) {
                    Icon(Icons.Default.Favorite, "En favoritos", tint = Coral,
                        modifier = Modifier.size(20.dp))
                }
                Pill("${r.distanceM} m", Coral, Color.White)
            }
        }
    }
}

//Mensaje de error con botón para volver a intentarlo
@Composable
fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, textAlign = TextAlign.Center, color = InkSoft)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Coral)) { Text("Reintentar") }
    }
}