package com.actiangent.sample.fusedlocation.di

import android.content.Context
import com.actiangent.sample.location.LocationProvider

object LocationModule {

    @Volatile
    private var locationProvider: LocationProvider? = null

    fun provideLocationProvider(context: Context): LocationProvider {
        synchronized(this) {
            return locationProvider ?: createLocationProvider(context)
        }
    }

    private fun createLocationProvider(context: Context) =
        LocationProvider(context).also { locationProvider = it }

}