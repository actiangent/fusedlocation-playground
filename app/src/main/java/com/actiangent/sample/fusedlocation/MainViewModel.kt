package com.actiangent.sample.fusedlocation

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actiangent.sample.location.LocationProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val location: String = "No location",
    val showPermissionDialog: Boolean = false
)

class MainViewModel(private val locationProvider: LocationProvider) : ViewModel() {

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

    fun getUpdatedLocation() = viewModelScope.launch {
        _locationString.update { locationProvider.awaitUpdatedLocation().asString() }
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
