package com.actiangent.sample.fusedlocation

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

data class MainUiState(
    val location: String = "No location",
    val showPermissionDialog: Boolean = false
)

class MainViewModel : ViewModel() {

    private val _locationString = MutableStateFlow("")
    private val _isPermissionDialogShow = MutableStateFlow(false)

    val uiState =
        combine(_locationString, _isPermissionDialogShow) { location, isPermissionDialogShow ->
            MainUiState(
                location,
                isPermissionDialogShow
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            initialValue = MainUiState()
        )

    fun updateLocation(location: Location) {
        _locationString.update { location.asString() }
    }

    fun showPermissionDialog() {
        _isPermissionDialogShow.update { true }
    }

    fun permissionDialogShown() {
        _isPermissionDialogShow.update { false }
    }

}

fun Location.asString(format: Int = Location.FORMAT_DEGREES): String {
    val latitude = Location.convert(latitude, format)
    val longitude = Location.convert(longitude, format)
    return "Location is: $latitude, $longitude"
}
