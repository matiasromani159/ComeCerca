package com.example.proyectocomecerca.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.proyectocomecerca.BottomBar
import com.example.proyectocomecerca.FoodViewModel
import com.example.proyectocomecerca.getLocation
import com.example.proyectocomecerca.model.FILTERS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(vm: FoodViewModel, nav: NavController) {
    val context = LocalContext.current
    val favorites by vm.favorites.collectAsState()
    val favIds = favorites.map { it.id }.toSet()            //ids de favoritos (para el corazón)
    val favById = favorites.associateBy { it.id }           // id -> Favorite (para las estrellas)

    // Lista final: filtro de cocina + ocultar donde no volverías
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ComeCerca") },
                actions = {
                    IconButton(onClick = { start() }) {
                        Icon(Icons.Default.Refresh, "Buscar de nuevo")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = { BottomBar(nav, "nearby") },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    list.filter { favById[it.id]?.wouldReturn != false }  //sin los "no volvería"
                        .randomOrNull()
                        ?.let { nav.navigate("detail/${it.id}") }
                },
                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                text = { Text("Sorpréndeme") }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = vm.hideBad,
                        onClick = { vm.hideBad = !vm.hideBad },
                        label = { Text("Ocultar donde no volvería") }
                    )
                }
                items(FILTERS) { label ->
                    FilterChip(
                        selected = vm.filter == label,
                        onClick = { vm.filter = label },
                        label = { Text(label) }
                    )
                }
            }
            val err = vm.error
            when {
                vm.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                err != null -> ErrorBox(err, onRetry = { start() })
                list.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay restaurantes de este tipo cerca")
                    }
                else -> LazyColumn(
                    // 88.dp abajo para que el botón flotante no tape la última tarjeta
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp,
                        end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(list, key = { it.id }) { r ->
                        Card(
                            onClick = { nav.navigate("detail/${r.id}") },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(r.name, style = MaterialTheme.typography.titleMedium)
                                    Text("${r.cuisine} · ${r.distanceM} m")
                                    favById[r.id]?.let { f ->
                                        Text(
                                            "★".repeat(f.rating) +
                                                    if (f.wouldReturn) " · Volverías" else " · No volverías",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (r.id in favIds) {
                                    Icon(Icons.Default.Favorite, "En favoritos",
                                        tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}