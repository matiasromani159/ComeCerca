package com.example.proyectocomecerca.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.proyectocomecerca.R

//Fuente Poppins: los .ttf están en res/font (nombres en minúscula y con _)
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

//Partimos de los estilos por defecto de Material y solo cambiamos la fuente (y algún tamaño/peso)
private val base = Typography()
private fun TextStyle.p(weight: FontWeight? = null) =
    copy(fontFamily = Poppins, fontWeight = weight ?: fontWeight)

val Typography = Typography(
    displayLarge = base.displayLarge.p(),
    displayMedium = base.displayMedium.p(),
    displaySmall = base.displaySmall.p(),
    headlineLarge = base.headlineLarge.p(FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(                 //títulos grandes de cabecera
        fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = base.headlineSmall.p(FontWeight.Bold),
    titleLarge = base.titleLarge.p(FontWeight.SemiBold),
    titleMedium = base.titleMedium.p(FontWeight.SemiBold),     //nombres de restaurantes y secciones
    titleSmall = base.titleSmall.p(FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.p(),
    bodyMedium = base.bodyMedium.p(),
    bodySmall = base.bodySmall.p(),
    labelLarge = base.labelLarge.p(FontWeight.SemiBold),       //texto de los botones
    labelMedium = base.labelMedium.p(FontWeight.SemiBold),
    labelSmall = base.labelSmall.p(FontWeight.SemiBold)
)