package com.actiangent.sample.fusedlocation

import android.app.Application
import com.actiangent.sample.fusedlocation.di.Injection

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        injection = Injection(applicationContext)
    }

    companion object {
        lateinit var injection: Injection
    }

}