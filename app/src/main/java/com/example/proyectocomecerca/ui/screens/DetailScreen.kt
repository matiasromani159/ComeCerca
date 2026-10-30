package com.example.proyectocomecerca.ui.screens


import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.proyectocomecerca.model.fakeRestaurants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(id: Long, onBack: () -> Unit) {
    val context = LocalContext.current
    val r = fakeRestaurants.find { it.id == id }

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
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (r == null) { Text("No encontrado"); return@Column }
            Text(r.name, style = MaterialTheme.typography.headlineMedium)
            Text(r.cuisine)
            r.address?.let { Text(it) }
            Button(
                onClick = { openRoute(context, r.lat, r.lon) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cómo llegar") }
            OutlinedButton(
                onClick = { /* DÍA 2: guardar en Room */ },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar en favoritos") }
        }
    }
}

// Abre Google Maps (o el navegador) con la ruta andando hasta el sitio
fun openRoute(context: Context, lat: Double, lon: Double) {
    val url = "https://www.google.com/maps/dir/?api=1" +
            "&destination=$lat,$lon&travelmode=walking"
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}