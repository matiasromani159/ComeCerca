package com.example.proyectocomecerca.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    val favIds = favorites.map { it.id }.toSet()

    fun searchHere() {
        vm.markLoading()
        getLocation(context,
            onResult = { lat, lon -> vm.search(lat, lon) },   // GPS -> API
            onFail = { vm.showError("No se pudo obtener tu ubicación. Activa el GPS.") })
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) searchHere()
        else vm.showError("Sin permiso de ubicación no puedo buscar restaurantes cerca de ti.")
    }

    fun start() {
        val granted = listOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
            .any { ContextCompat.checkSelfPermission(context, it) ==
                    PackageManager.PERMISSION_GRANTED }
        if (granted) searchHere()
        else launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    LaunchedEffect(Unit) { if (vm.restaurants.isEmpty()) start() }  // no repetir al volver

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
        bottomBar = { BottomBar(nav, "nearby") }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            LazyRow(   // chips de cocina
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(FILTERS) { label ->
                    FilterChip(
                        selected = vm.filter == label,
                        onClick = { vm.filter = label },   // botón funcional
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
                vm.visible.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay restaurantes de este tipo cerca")
                    }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(vm.visible, key = { it.id }) { r ->
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