package org.noiseplanet.noisecapture.permission.delegate

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.PermissionRequestException
import org.noiseplanet.noisecapture.permission.util.checkPermissions
import org.noiseplanet.noisecapture.permission.util.openAppSettingsPage
import org.noiseplanet.noisecapture.permission.util.providePermissions

internal class LocationForegroundPermissionDelegate(
    private val context: Context,
    private val activity: Lazy<Activity>,
) : PermissionDelegate {

    override suspend fun getPermissionState(): PermissionState {
        return activity.value.checkPermissions(fineLocationPermissions)
    }

    override suspend fun providePermission() {
        activity.value.providePermissions(fineLocationPermissions) {
            throw PermissionRequestException(
                it.localizedMessage ?: "Failed to request foreground location permission"
            )
        }
    }

    override fun openSettingPage() {
        context.openAppSettingsPage(Permission.LOCATION_FOREGROUND)
    }
}

private val fineLocationPermissions: List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    } else {
        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
