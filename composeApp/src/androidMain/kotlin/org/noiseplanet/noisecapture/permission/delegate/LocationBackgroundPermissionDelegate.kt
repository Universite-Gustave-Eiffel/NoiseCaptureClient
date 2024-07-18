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

internal class LocationBackgroundPermissionDelegate(
    private val context: Context,
    private val activity: Lazy<Activity>,
    private val locationForegroundPermissionDelegate: PermissionDelegate,
) : PermissionDelegate {

    override fun getPermissionState(): PermissionState {
        return when (locationForegroundPermissionDelegate.getPermissionState()) {
            PermissionState.GRANTED ->
                checkPermissions(context, backgroundLocationPermissions)

            else -> PermissionState.NOT_DETERMINED
        }
    }

    override fun providePermission() {
        activity.value.providePermissions(backgroundLocationPermissions) {
            throw PermissionRequestException(
                it.localizedMessage ?: "Failed to request background location permission"
            )
        }
        getPermissionState()
    }

    override fun openSettingPage() {
        context.openAppSettingsPage(Permission.LOCATION_BACKGROUND)
    }
}

private val backgroundLocationPermissions: List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        emptyList()
    }
