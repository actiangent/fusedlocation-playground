package com.actiangent.sample.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

/*
 * Still should request permission in Activity
 */

@SuppressLint("MissingPermission")
@OptIn(ExperimentalCoroutinesApi::class)
class LocationProvider(context: Context) {

    internal val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    suspend fun awaitLastLocation(): Location = suspendCancellableCoroutine { continuation ->
        fusedLocationClient.lastLocation.addOnSuccessListener { location -> // location still can hold null value
            location?.let { continuation.resume(it, null) }
        }.addOnFailureListener { exception ->
            continuation.cancel(exception)
        }
    }

    suspend fun awaitUpdatedLocation(
        priority: Int = Priority.PRIORITY_BALANCED_POWER_ACCURACY
    ): Location = suspendCancellableCoroutine { continuation ->
        val callback = object : LocationCallback() {
            // keep updating when enabling location without leaving app
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { continuation.resume(it, null) }
                // after getting latest location remove this callback
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        val onFailureListener: (Exception) -> Unit =
            { exception -> continuation.cancel(exception) }

        fusedLocationClient.getCurrentLocation(priority, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(location, null)
                } else {
                    fusedLocationClient.requestLocationUpdates(
                        createLocationRequest(),
                        callback,
                        Looper.getMainLooper()
                    ).addOnFailureListener { onFailureListener(it) }
                }
            }.addOnFailureListener { onFailureListener(it) }
    }
}

// Send location updates to the consumer
@SuppressLint("MissingPermission")
fun LocationProvider.locationFlow(intervalMillis: Long) = callbackFlow<Location> {
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                try {
                    // Send location to the flow
                    trySend(location)
                } catch (t: Throwable) {
                    // Location couldn't be sent to the flow
                }
            }
        }
    }

    // Register the callback
    fusedLocationClient.requestLocationUpdates(
        createLocationRequest(intervalMillis = intervalMillis),
        callback,
        Looper.getMainLooper()
    ).addOnFailureListener { e ->
        close(e) // in case of error, close the Flow
    }

    // Wait for the consumer to cancel the coroutine and unregister
    // the callback. This suspends the coroutine until the Flow is closed.
    awaitClose {
        // Clean up code goes here
        fusedLocationClient.removeLocationUpdates(callback)
    }
}

private fun createLocationRequest(
    priority: Int = Priority.PRIORITY_BALANCED_POWER_ACCURACY,
    intervalMillis: Long = 1000L,
    waitForAccurateLocation: Boolean = false
) = LocationRequest.Builder(priority, intervalMillis)
    .setWaitForAccurateLocation(waitForAccurateLocation)
    .build()