package com.actiangent.sample.fusedlocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.actiangent.sample.fusedlocation.di.Injection

class ViewModelFactory(private val injection: Injection) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(injection.locationProvider)
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        } as T
    }
}