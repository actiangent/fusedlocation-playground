package com.actiangent.sample.fusedlocation

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.actiangent.sample.fusedlocation.ui.component.RationalePermissionDialog
import com.actiangent.sample.fusedlocation.ui.theme.FusedLocationSampleTheme
import com.actiangent.sample.fusedlocation.util.checkBuildVersion
import com.actiangent.sample.fusedlocation.util.hasPermission
import com.actiangent.sample.fusedlocation.util.openAppSettings
import com.actiangent.sample.fusedlocation.util.shouldShowRequestPermissionsRationale
import com.actiangent.sample.location.LocationProvider
import kotlinx.coroutines.launch

const val COARSE_LOCATION_PERMISSION = Manifest.permission.ACCESS_COARSE_LOCATION
const val FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

class MainActivity : ComponentActivity() {

    private lateinit var locationProvider: LocationProvider

    private val viewModel: MainViewModel by viewModels()

    private val locationPermission = arrayOf(COARSE_LOCATION_PERMISSION, FINE_LOCATION_PERMISSION)
    private val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value == true }) {
            viewModel.permissionDialogShown()

            val onAwaitLocationUpdate: suspend () -> Unit = {
                viewModel.updateLocation(locationProvider.awaitUpdatedLocation())
            }
            lifecycleScope.launch { onAwaitLocationUpdate() }
        } else {
            viewModel.showPermissionDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationProvider = LocationProvider(this)

        setContent {
            FusedLocationSampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val uiState by viewModel.uiState.collectAsState()

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = uiState.location)
                    }

                    if (uiState.showPermissionDialog) {
                        if (checkBuildVersion(Build.VERSION_CODES.M)) {
                            RationalePermissionDialog(
                                onConfirm = { requestLocationPermission.launch(locationPermission) },
                                onDismiss = { viewModel.permissionDialogShown() },
                                title = "Access location",
                                text = "This app need access to location, please grant the permission",
                                onConfirmText = "Grant",
                                onDismissText = "Dismiss",
                                isPermanentlyDeclined = !shouldShowRequestPermissionsRationale(
                                    COARSE_LOCATION_PERMISSION, FINE_LOCATION_PERMISSION
                                ),
                                requestPermissionRationaleText = "Please enable location permission on the app settings",
                                goToAppSetting = { openAppSettings() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (!hasPermission(COARSE_LOCATION_PERMISSION) and !hasPermission(FINE_LOCATION_PERMISSION)) {
            // Permission Check is only available from API 23 (from Marshmallow).
            // You don't need to ask for Permission on Api < 23
            // as they are automatically granted on App Install.
            if (checkBuildVersion(Build.VERSION_CODES.M)) {
                requestLocationPermission.launch(locationPermission)
            }
        }

    }
}

