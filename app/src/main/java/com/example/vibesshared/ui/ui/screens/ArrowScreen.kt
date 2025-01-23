package com.example.vibesshared.ui.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.vibesshared.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

@Composable
fun ArrowScreen(navController: NavHostController) {
    val context = LocalContext.current
    var targetLocation by remember { mutableStateOf<LatLng?>(null) }
    var searchAddress by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var bearing by remember { mutableFloatStateOf(0f) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var displayAddress by remember { mutableStateOf<String?>(null) }
    var distanceToTarget by remember { mutableStateOf<String?>(null) }

    // Sensor-related variables
    val rotationMatrix = FloatArray(9)
    val orientationAngles = FloatArray(3)
    var compassBearing by remember { mutableFloatStateOf(0f) }

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // Function to calculate bearing
    fun calculateBearing(current: Location?, target: LatLng?, onBearingCalculated: (Float?) -> Unit) {
        current?.let { currentLocation ->
            target?.let { targetLocation ->
                val targetLoc = Location("dummy").apply {
                    latitude = targetLocation.latitude
                    longitude = targetLocation.longitude
                }
                onBearingCalculated(currentLocation.bearingTo(targetLoc))
            } ?: onBearingCalculated(null)
        } ?: onBearingCalculated(null)
    }

    // Function to calculate distance
    @SuppressLint("DefaultLocale")
    fun calculateDistance(current: Location?, target: LatLng?): String? {
        if (current == null || target == null) return null

        val results = FloatArray(1)
        Location.distanceBetween(
            current.latitude, current.longitude,
            target.latitude, target.longitude,
            results
        )

        val distanceInMeters = results[0]
        val distanceInMiles = distanceInMeters * 0.000621371 // Convert meters to miles

        return String.format("%.2f", distanceInMiles) // Format to two decimal places
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            requestLocationUpdates(fusedLocationClient, context) { location ->
                currentLocation = location
                if (targetLocation != null) {
                    coroutineScope.launch {
                        calculateBearing(location, targetLocation) { calculatedBearing ->
                            bearing = calculatedBearing ?: 0f
                        }
                        distanceToTarget = calculateDistance(location, targetLocation)
                    }
                }
            }
        } else {
            // Handle permission denial
        }
    }

    fun handleLocationPermissions(
        context: Context,
        fusedLocationClient: FusedLocationProviderClient,
        locationPermissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
        onLocationUpdate: (Location) -> Unit
    ) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                requestLocationUpdates(fusedLocationClient, context, onLocationUpdate)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // Show rationale if needed
            }

            else -> {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    LaunchedEffect(key1 = true) {
        handleLocationPermissions(
            context,
            fusedLocationClient,
            locationPermissionLauncher
        ) { location ->
            currentLocation = location
            if (targetLocation != null) {
                coroutineScope.launch {
                    calculateBearing(location, targetLocation) { calculatedBearing ->
                        bearing = calculatedBearing ?: 0f
                    }
                    distanceToTarget = calculateDistance(location, targetLocation)
                }
            }
        }
    }

    DisposableEffect(context) {
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    compassBearing = azimuth
                    Log.d("Compass", "Compass Bearing: $compassBearing")
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Handle accuracy changes if needed
            }
        }

        rotationVectorSensor?.let {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    @Suppress("DEPRECATION")
    fun geocodeAddress(addressString: String) {
        coroutineScope.launch {
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                val addresses = geocoder.getFromLocationName(addressString, 5)
                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    targetLocation = LatLng(location.latitude, location.longitude)
                    displayAddress = addresses[0].getAddressLine(0)
                    currentLocation?.let { currentLoc ->
                        calculateBearing(currentLoc, targetLocation) { calculatedBearing ->
                            bearing = calculatedBearing ?: 0f
                        }
                        distanceToTarget = calculateDistance(currentLoc, targetLocation)
                    }
                } else {
                    targetLocation = null
                    displayAddress = "Address Not Found"
                    Log.d("geocoding", "No addresses found for: $addressString")
                }
            } catch (e: Exception) {
                targetLocation = null
                displayAddress = when (e) {
                    is IOException -> "Geocoding Error: Check internet connection"
                    is IllegalArgumentException -> "Invalid Address"
                    else -> "Geocoding Error"
                }
                Log.e("geocoding", "Geocoding error", e)
            }
        }
    }

    fun resetSearch() {
        searchAddress = ""
        targetLocation = null
        displayAddress = null
        bearing = 0f
        distanceToTarget = null
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val finalBearing = if (targetLocation != null && currentLocation != null) {
            bearing - compassBearing
        } else {
            0f
        }


        when {
            targetLocation != null && currentLocation != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center) {
                        // Main arrow pointing to the target location
                        Image(
                            painter = painterResource(id = R.drawable.finger_up_tp),
                            contentDescription = "Navigation Arrow",
                            modifier = Modifier
                                .size(250.dp)
                                .rotate(finalBearing),
                            contentScale = ContentScale.Fit

                        )
                    }
                    displayAddress?.let {
                        Text(it, modifier = Modifier.padding(top = 8.dp))
                    }
                    // Display the distance
                    distanceToTarget?.let {
                        Text(
                            text = "$it miles",
                            modifier = Modifier.padding(top = 8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            targetLocation == null && searchAddress.isNotBlank() -> {
                Text("Location Not Found", color = MaterialTheme.colorScheme.error)
            }
            else -> {
                Image(
                    painter = painterResource(id = R.drawable.finger_up_tp),
                    contentDescription = "Finger Point",
                    modifier = Modifier
                        .size(250.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        OutlinedTextField(
            value = searchAddress,
            onValueChange = { searchAddress = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("Enter address") }
        )
        Row {
            Button(onClick = { geocodeAddress(searchAddress) }) {
                Text("Go")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { resetSearch() }) {
                Text("Reset")
            }
        }
    }
}

private fun requestLocationUpdates(
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,
    onLocationChange: (Location) -> Unit
) {
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                onLocationChange(location)
            }
        }
    }
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (securityException: SecurityException) {
            Log.e("Location", "Security exception when requesting location updates", securityException)
            // Handle the exception appropriately, e.g., inform the user
        }
    }
}