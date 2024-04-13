package com.example.sky_map

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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


class MainActivity : ComponentActivity(), SensorEventListener {
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationTextView: TextView

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private lateinit var sensorsTextView: TextView

    private var accelerometerData = FloatArray(3)
    private var magnetometerData = FloatArray(3)

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize sensor manager and sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!


        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        locationTextView = TextView(this)
        layout.addView(locationTextView)

        sensorsTextView = TextView(this)
        layout.addView(sensorsTextView)

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

    override fun onResume() {
        super.onResume()
        // Register sensor listeners
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listeners to conserve battery
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Update accelerometer and magnetometer data
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> System.arraycopy(event.values, 0, accelerometerData, 0, 3)
            Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(event.values, 0, magnetometerData, 0,3)
        }
        // Calculate orientation
        var rotationMatrix = FloatArray(9)
        var success = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerData, magnetometerData)
        if (success) {
            var orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            var azimuthRadians = orientation[0]
            var azimuthDegrees = Math.toDegrees(azimuthRadians.toDouble()).toFloat()
            // TODO Use azimuthDegrees to update your UI or perform other actions
            sensorsTextView.text = "Azimuth: $azimuthDegrees"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // No implementation needed
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