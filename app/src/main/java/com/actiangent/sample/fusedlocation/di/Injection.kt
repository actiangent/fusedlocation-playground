package com.actiangent.sample.fusedlocation.di

import android.content.Context
import com.actiangent.sample.location.LocationProvider

class Injection(context: Context) {

    val locationProvider = LocationModule.provideLocationProvider(context)

}