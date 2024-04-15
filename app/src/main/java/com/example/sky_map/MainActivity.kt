package com.example.sky_map

import com.example.sky_map.CelestialResponse
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
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.Serializable

@Serializable
data class CelestialResponse(
    val data: Data
)

@Serializable
data class Data(
    val dates: Dates,
    val observer: Observer,
    val table: Table
)

@Serializable
data class Dates(
    val from: String,
    val to: String
)

@Serializable
data class Observer(
    val location: CLocation
)

@Serializable
data class CLocation(
    val longitude: Double,
    val latitude: Double,
    val elevation: Int
)

@Serializable
data class Table(
    val header: List<String>,
    val rows: List<Row>
)

@Serializable
data class Row(
    val entry: Entry
)

@Serializable
data class Entry(
    val id: String,
    val name: String,
    val cells: List<Cell> = emptyList()
)

@Serializable
data class Cell(
    val date: String,
    val id: String,
    val distance: Distance,
    val position: Position
)

@Serializable
data class Distance(
    val fromEarth: DistanceData
)

@Serializable
data class DistanceData(
    val au: String,
    val km: String
)

@Serializable
data class Position(
    val horizontal: Horizontal,
    val equatorial: Equatorial,
    val constellation: Constellation,
    val extralnfo: ExtraInfo
)

@Serializable
data class Horizontal(
    val altitude: Angle,
    val azimuth: Angle
)

@Serializable
data class Equatorial(
    val rightAscension: HourAngle,
    val declination: Angle
)

@Serializable
data class Constellation(
    val id: String,
    val short: String,
    val name: String
)

@Serializable
data class ExtraInfo(
    val elongation: Double,
    val magnitude: Double,
    val phase: Phase
)

@Serializable
data class Angle(
    @Serializable(with = AngleSerializer::class)
    val degrees: Double,
    val string: String
)

@Serializable
data class HourAngle(
    @Serializable(with = HourAngleSerializer::class)
    val hours: Double,
    val string: String
)

@Serializable
data class Phase(
    val angle: Double,
    val fraction: String,
    val string: String
)

object AngleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Angle", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Double {
        val value = decoder.decodeString()
        return value.dropLast(1).toDouble() // Removing the last character (') and converting to Double
    }
}

object HourAngleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HourAngle", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Double {
        val value = decoder.decodeString()
        return value.dropLast(1).toDouble() // Removing the last character (') and converting to Double
    }
}


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

    private val client = OkHttpClient()
    private val APP_URL = "https://api.astronomyapi.com/api/v2/bodies/positions"
    private val APP_SECRET = "388d813bb29cc54f9d9817cb4b3ad6ac2fb412b23b8cf0c89e291f2587783fbdc4d0b447a4fff2994539ae511cd17f6d8097ac42e0ab4f81bde74a5cd6287bfdf6620d4ef4520ab5cf0e3ef95d89ac3e9e99488918342a86ab9cee583c091bd6fe362dd6cd59e58c2e551ecd140f3670"
    private val APP_ID = "d7aa8d3e-e68c-4b0f-b2e6-f43efd799021"
    private lateinit var apiTextView: TextView

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

        apiTextView = TextView(this)
        layout.addView(apiTextView)

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
                    fetchDataFromAstronomyAPI(location)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchDataFromAstronomyAPI(location: Location) {
        val elevation = "0"
        val today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        val credentialsBase64 = Base64.getEncoder().encodeToString("$APP_ID:$APP_SECRET".toByteArray())
        val url = "$APP_URL?latitude=${location.latitude.toString()}" +
                "&longitude=${location.longitude.toString()}" +
                "&elevation=${elevation}" +
                "&from_date=$today" +
                "&to_date=$today" +
                "&time=$time"

        val request = Request.Builder()
            .url(url)
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Authorization", "Basic $credentialsBase64")
            .build()

        client.newCall(request).enqueue(object: Callback {
            @SuppressLint("SetTextI18n")
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API Request", "Failure: ${e.message}")
                runOnUiThread {
                    apiTextView.text = "Failed to fetch data: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                // Process the response body (e.g., parse JSON)
                if (response.isSuccessful) {
                    runOnUiThread {
                        if (responseBody != null) {
                            processResponse(responseBody)
                        }
                    }
                    Log.i("Network Test", "Google reachable, internet is working")
                } else {
                    Log.e("Network Test", "Failed to reach Google")
                }
            }
        })
    }

    fun processResponse(responseBody: String) {
        val json = Json { ignoreUnknownKeys = true } // Allows us to ignore properties we don't need
        try {
            val celestialResponse = json.decodeFromString<CelestialResponse>(responseBody)
            runOnUiThread {
                apiTextView.text = celestialResponse.toString()
            }
        } catch (e: SerializationException) {
            Log.e("processResponse", "SerializationException parsing JSON", e)
            runOnUiThread {
                apiTextView.text = "SerializationException parsing JSON: ${e.localizedMessage}"
            }
        } catch (e: Exception) {
            Log.e("processResponse", "Error parsing JSON", e)
            runOnUiThread {
                apiTextView.text = "Error parsing JSON: ${e.localizedMessage}"
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