package org.noiseplanet.noisecapture.permission.delegate

import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.openNSUrl
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted

internal class LocationForegroundPermissionDelegate : PermissionDelegate {

    private var locationManager = CLLocationManager()

    override suspend fun getPermissionState(): PermissionState {
        return when (locationManager.authorizationStatus()) {
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusRestricted,
            -> PermissionState.GRANTED

            kCLAuthorizationStatusNotDetermined -> PermissionState.NOT_DETERMINED
            kCLAuthorizationStatusDenied -> PermissionState.DENIED
            else -> PermissionState.NOT_DETERMINED
        }
    }

    override suspend fun providePermission() {
        locationManager.requestWhenInUseAuthorization()
    }

    override fun openSettingPage() {
        openNSUrl("App-Prefs:Privacy&path=LOCATION")
    }
}
