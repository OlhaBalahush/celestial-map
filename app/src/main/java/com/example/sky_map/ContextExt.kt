package com.example.sky_map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat

fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED
}