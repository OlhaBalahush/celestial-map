package com.example.sky_map

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.*


class MainActivity : ComponentActivity() {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationTextView: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        locationTextView = TextView(this)
        layout.addView(locationTextView)

        setContentView(layout)

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        if (!this.hasLocationPermission()) {
            onLocationPermissionDenied()
            return
        }

        locationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onLocationAvailable(location)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onLocationAvailable(location: Location) {
        println("Latitude ${location.latitude}")
        println("Longitude ${location.longitude}")
        val locationString = "Latitude: ${location.latitude}, Longitude: ${location.longitude}"
        locationTextView.text = locationString
    }

    private fun onLocationPermissionDenied() {
       Toast.makeText(this, "Location permission is required to display your current location.", Toast.LENGTH_SHORT).show()
        requestLocationPermission();
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Permission granted, you can proceed with location-related tasks
            } else {
                Toast.makeText(this, "Location permission is required to display your current location.", Toast.LENGTH_SHORT). show()
            }
        }
    }
}