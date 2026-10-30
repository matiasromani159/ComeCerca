package com.example.proyectocomecerca.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Nuestros colores (terracota). Sin dynamicColor, para que Android 12+ no los ignore.
private val Esquema = lightColorScheme(
    primary = Color(0xFFA63D2A), onPrimary = Color.White,   // terracota
    secondaryContainer = Color(0xFFF6D9CF),                 // chips seleccionados
    onSecondaryContainer = Color(0xFF5A1D10),
    background = Color(0xFFFBF7F2), surface = Color.White,
    surfaceContainer = Color(0xFFF3EBE3)                    // barra inferior
)

@Composable
fun ProyectoComeCercaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Esquema,
        typography = Typography,   // viene de Type.kt, que ya trae la plantilla
        content = content
    )
}