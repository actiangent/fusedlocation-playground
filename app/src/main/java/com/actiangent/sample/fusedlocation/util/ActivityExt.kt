package com.actiangent.sample.fusedlocation.util

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

fun Activity.openAppSettings() {
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null)
    ).also(::startActivity)
}

fun Activity.hasPermission(permission: String): Boolean =
    ActivityCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.shouldShowRequestPermissionsRationale(
    vararg permissions: String
) = permissions.all { shouldShowRequestPermissionRationale(it) }

@RequiresApi(Build.VERSION_CODES.M)
fun ComponentActivity.requestPermission(
    permission: String,
    callback: (Boolean) -> Unit
) = registerForActivityResult(
    ActivityResultContracts.RequestPermission(),
    callback
).launch(permission)

@RequiresApi(Build.VERSION_CODES.M)
fun ComponentActivity.requestPermissions(
    vararg permissions: String,
    callback: (Set<Map.Entry<String, Boolean>>) -> Unit
) = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { callback(it.entries) }.launch(arrayOf(*permissions))

fun checkBuildVersion(minSdk: Int): Boolean = (minSdk <= Build.VERSION.SDK_INT)