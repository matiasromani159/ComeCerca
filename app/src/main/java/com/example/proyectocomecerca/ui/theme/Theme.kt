package com.example.proyectocomecerca.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//Tema propio: asigna nuestra paleta (Color.kt) a los "huecos" de Material 3.
//Así los componentes (botones, chips, switch...) salen con nuestros colores sin tocarlos uno a uno.
private val Esquema = lightColorScheme(
    primary = Teal, onPrimary = Color.White,
    primaryContainer = TealSoft, onPrimaryContainer = Teal,
    secondary = Coral, onSecondary = Color.White,
    secondaryContainer = TealSoft, onSecondaryContainer = Teal,
    tertiary = Coral, onTertiary = Color.White,
    background = Page, onBackground = Ink,
    surface = Color.White, onSurface = Ink,
    surfaceVariant = Tile, onSurfaceVariant = InkSoft,
    surfaceContainer = Tile, surfaceContainerHigh = Tile, surfaceContainerHighest = Mist,
    outline = Mist,
    error = Coral, onError = Color.White
)

//Envuelve toda la app (se usa en MainActivity). Sin dynamicColor:
//si no, en Android 12+ el móvil pondría los colores del fondo de pantalla.
@Composable
fun ProyectoComeCercaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Esquema,
        typography = Typography,   //Poppins, viene de Type.kt
        content = content
    )
}