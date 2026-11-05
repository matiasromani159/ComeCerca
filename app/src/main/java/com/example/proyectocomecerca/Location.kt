package com.example.proyectocomecerca

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

//Pide UNA ubicación. No devuelve nada directamente: avisa por "callbacks"
//(onResult si va bien, onFail si no), porque el GPS tarda.
@SuppressLint("MissingPermission")   //el permiso ya se comprobó en NearbyScreen
fun getLocation(context: Context, onResult: (Double, Double) -> Unit, onFail: () -> Unit) {
    val client = LocationServices.getFusedLocationProviderClient(context)  //GPS + wifi + red
    client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)       //ubicación precisa
        .addOnSuccessListener { loc ->
            if (loc != null) {
                onResult(loc.latitude, loc.longitude)
            } else {
                client.lastLocation                    //plan B: última ubicación conocida
                    .addOnSuccessListener { last ->
                        if (last != null) onResult(last.latitude, last.longitude) else onFail()
                    }
                    .addOnFailureListener { onFail() }
            }
        }
        .addOnFailureListener { onFail() }
}