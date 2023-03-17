package com.actiangent.sample.fusedlocation

import android.location.Address
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.actiangent.sample.location.LocationProvider
import com.actiangent.sample.location.getAddress
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val EMPTY_LOCATION = Location(null)

data class MainUiState(
    val location: Location = EMPTY_LOCATION,
    val address: Address? = null,
    val showPermissionDialog: Boolean = false
)

class MainViewModel(
    private val locationProvider: LocationProvider,
    private val geocoder: Geocoder
) : ViewModel() {

    private val _location = MutableStateFlow(EMPTY_LOCATION)
    private val _isPermissionDialogShow = MutableStateFlow(false)

    val uiState =
        combine(_location, _isPermissionDialogShow) { location, isPermissionDialogShow ->
            MainUiState(
                location,
                geocoder.getAddress(location),
                isPermissionDialogShow
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            initialValue = MainUiState()
        )

    fun getUpdatedLocation() = viewModelScope.launch {
        _location.update { locationProvider.awaitUpdatedLocation() }
    }

    fun showPermissionDialog() {
        _isPermissionDialogShow.update { true }
    }

    fun permissionDialogShown() {
        _isPermissionDialogShow.update { false }
    }

}
