package com.example.proyectocomecerca.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectocomecerca.R
import com.example.proyectocomecerca.ui.theme.Gold
import com.example.proyectocomecerca.ui.theme.GreyStar
import com.example.proyectocomecerca.ui.theme.Ink
import com.example.proyectocomecerca.ui.theme.Teal
import com.example.proyectocomecerca.ui.theme.Tile

//Piezas de diseño que se repiten en varias pantallas.

//Foto ilustrativa según la cocina (no es la foto real del restaurante).
//Las imágenes están en res/drawable con estos nombres.
@DrawableRes
fun cuisinePhoto(cuisine: String): Int = when (cuisine) {
    "Todas" -> R.drawable.food_todas
    "Mediterránea" -> R.drawable.food_mediterranea
    "Japonesa" -> R.drawable.food_japonesa
    "Italiana" -> R.drawable.food_italiana
    "Española" -> R.drawable.food_espanola
    "Hamburguesas" -> R.drawable.food_hamburguesas
    "China" -> R.drawable.food_china
    "Mexicana" -> R.drawable.food_mexicana
    else -> R.drawable.food_generica          //"Otras" y "Sin especificar"
}

//Muestra la foto de la cocina recortada con la forma que le pasemos (círculo, esquinas redondas...)
@Composable
fun FoodPhoto(cuisine: String, size: Dp, shape: Shape, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(cuisinePhoto(cuisine)),
        contentDescription = null,              //decorativa: el nombre ya se lee al lado
        contentScale = ContentScale.Crop,       //rellena el hueco sin deformarse
        modifier = modifier.size(size).clip(shape)
    )
}

//Cabecera verde con las esquinas de abajo redondeadas. Llega hasta arriba del todo
//(detrás de la hora y la batería), por eso lleva statusBarsPadding.
//Todo lo que va dentro sale en blanco (LocalContentColor).
@Composable
fun Header(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Teal, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .statusBarsPadding()
            .padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 24.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) { content() }
    }
}

//Título de sección ("Categorías", "Cerca de ti"...)
@Composable
fun SectionTitle(text: String) {
    Text(
        text,
        Modifier.padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        color = Teal
    )
}

//Etiqueta pequeña con forma de píldora (distancia, "Volvería"...)
@Composable
fun Pill(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = fg,
        style = MaterialTheme.typography.labelSmall
    )
}

//Estrellas: doradas las puestas y grises las que faltan hasta 5
@Composable
fun Stars(rating: Int, fontSize: TextUnit = 14.sp) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(color = Gold)) { append("★".repeat(rating)) }
            withStyle(SpanStyle(color = GreyStar)) { append("★".repeat(5 - rating)) }
        },
        fontSize = fontSize,
        letterSpacing = 1.sp
    )
}

//Baldosa de categoría: foto redonda + nombre. Seleccionada = fondo verde.
@Composable
fun CategoryTile(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .widthIn(min = 88.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Teal else Tile)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FoodPhoto(label, 54.dp, CircleShape, Modifier.border(3.dp, Color.White, CircleShape))
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            color = if (selected) Color.White else Ink,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }
}