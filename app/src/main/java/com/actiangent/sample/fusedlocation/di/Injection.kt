package com.actiangent.sample.fusedlocation.di

import android.content.Context
import android.location.Geocoder

class Injection(context: Context) {

    val locationProvider = LocationModule.provideLocationProvider(context)
    val geocoder = Geocoder(context)

}