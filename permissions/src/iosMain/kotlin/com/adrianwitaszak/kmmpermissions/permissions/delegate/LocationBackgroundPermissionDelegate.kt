package com.adrianwitaszak.kmmpermissions.permissions.delegate

import com.adrianwitaszak.kmmpermissions.permissions.model.PermissionState
import com.adrianwitaszak.kmmpermissions.permissions.util.openAppSettingsPage
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusDenied

internal class LocationBackgroundPermissionDelegate(
    private val locationForegroundPermissionDelegate: PermissionDelegate,
) : PermissionDelegate {
    override suspend fun getPermissionState(): PermissionState {
        val foregroundPermissionStatus =
            locationForegroundPermissionDelegate.getPermissionState()
        return when (foregroundPermissionStatus) {
            PermissionState.GRANTED -> checkBackgroundLocationPermission()
            else
            -> foregroundPermissionStatus
        }
    }

    override suspend fun providePermission() {
        CLLocationManager().requestAlwaysAuthorization()
    }

    override fun openSettingPage() {
        openAppSettingsPage()
    }

    private fun checkBackgroundLocationPermission(): PermissionState {
        return when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways -> PermissionState.GRANTED
            kCLAuthorizationStatusDenied -> PermissionState.DENIED
            else -> PermissionState.NOT_DETERMINED
        }
    }
}
