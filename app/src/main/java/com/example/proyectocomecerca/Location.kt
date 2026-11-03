package com.example.proyectocomecerca

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

// Pide UNA ubicación al sistema (GPS / red). Solo se llama con el permiso concedido.
@SuppressLint("MissingPermission")
fun getLocation(context: Context, onResult: (Double, Double) -> Unit, onFail: () -> Unit) {
    LocationServices.getFusedLocationProviderClient(context)
        .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
        .addOnSuccessListener { loc ->
            if (loc != null) onResult(loc.latitude, loc.longitude) else onFail()
        }
        .addOnFailureListener { onFail() }
}