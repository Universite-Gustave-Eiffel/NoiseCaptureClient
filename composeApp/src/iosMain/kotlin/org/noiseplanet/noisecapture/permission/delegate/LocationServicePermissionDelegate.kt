package org.noiseplanet.noisecapture.permission.delegate

import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.openNSUrl
import platform.CoreLocation.CLLocationManager

internal class LocationServicePermissionDelegate : PermissionDelegate {

    private val locationManager = CLLocationManager()

    override suspend fun getPermissionState(): PermissionState {
        return if (locationManager.locationServicesEnabled()) {
            PermissionState.GRANTED
        } else {
            PermissionState.DENIED
        }
    }

    override suspend fun providePermission() {
        openSettingPage()
    }

    override fun openSettingPage() {
        openNSUrl("App-Prefs:Privacy&path=LOCATION")
    }
}
