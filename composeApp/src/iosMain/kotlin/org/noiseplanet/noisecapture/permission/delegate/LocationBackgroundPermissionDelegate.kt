package org.noiseplanet.noisecapture.permission.delegate

import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.openNSUrl
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusDenied

internal class LocationBackgroundPermissionDelegate(
    private val locationForegroundPermissionDelegate: PermissionDelegate,
) : PermissionDelegate {

    override fun getPermissionState(): PermissionState {
        val foregroundPermissionStatus =
            locationForegroundPermissionDelegate.getPermissionState()
        return when (foregroundPermissionStatus) {
            PermissionState.GRANTED -> checkBackgroundLocationPermission()
            else
            -> foregroundPermissionStatus
        }
    }

    override fun providePermission() {
        CLLocationManager().requestAlwaysAuthorization()
    }

    override fun openSettingPage() {
        openNSUrl("App-Prefs:Privacy&path=LOCATION")
    }

    private fun checkBackgroundLocationPermission(): PermissionState {
        return when (CLLocationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways -> PermissionState.GRANTED
            kCLAuthorizationStatusDenied -> PermissionState.DENIED
            else -> PermissionState.NOT_DETERMINED
        }
    }
}
