package com.example.healthsurveyappandroid.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume


interface LocationClient {
    suspend fun getLocation(highAccuracy: Boolean): Location?
}

class LocationClientImpl(context: Context) : LocationClient {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getLocation(highAccuracy: Boolean): Location? {
        val priority = if (highAccuracy)
            Priority.PRIORITY_HIGH_ACCURACY
        else
            Priority.PRIORITY_BALANCED_POWER_ACCURACY

        Log.d("LocationClient", "Trying getCurrentLocation with priority $priority")

        return try {
            val cancellationToken = CancellationTokenSource()
            val location =
                fusedLocationClient.getCurrentLocation(priority, cancellationToken.token).await()

            if (location != null) {
                Log.d("LocationClient", "Location from getCurrentLocation: $location")
                location
            } else {
                Log.d(
                    "LocationClient",
                    "getCurrentLocation returned null. Falling back to requestLocationUpdates."
                )
                getLastLocationViaUpdates(priority)
            }
        } catch (e: Exception) {
            Log.e("LocationClient", "getCurrentLocation failed: ${e.message}")
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastLocationViaUpdates(priority: Int): Location? {
        return suspendCancellableCoroutine { cont ->
            val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                LocationRequest.Builder(priority, 10000L)
                    .setMinUpdateIntervalMillis(5000L)
                    .setWaitForAccurateLocation(true)
                    .build()
            } else {
                LocationRequest.create().apply {
                    interval = 10000L
                    fastestInterval = 5000L
                    this.priority = priority
                }
            }

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    cont.resume(result.lastLocation)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

            fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

            cont.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(callback)
            }
        }
    }
}
