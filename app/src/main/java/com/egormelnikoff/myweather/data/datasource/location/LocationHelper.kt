package com.egormelnikoff.myweather.data.datasource.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.egormelnikoff.myweather.WeatherApplication
import com.egormelnikoff.myweather.data.Result
import com.egormelnikoff.myweather.model.Place
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(
    private val locationClient: FusedLocationProviderClient,
    private val application: WeatherApplication
) {

    suspend fun getCurrentLocationPlace(): Place? {
        val location = getCurrentLocation()
        if (location != null) {
            return when (val place = application.repository.getPlaceByLocation(location)) {
                is Result.Error -> {
                    null
                }

                is Result.Success -> {
                    place.data
                }
            }

        }
        return null
    }

    private suspend fun getCurrentLocation(): Location? {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!hasFineLocationPermission || !hasCoarseLocationPermission || !isGpsEnabled) {
            return null
        }


        return suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()

            locationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (continuation.isActive) {
                    continuation.resume(location)
                }
            }.addOnFailureListener {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }
}