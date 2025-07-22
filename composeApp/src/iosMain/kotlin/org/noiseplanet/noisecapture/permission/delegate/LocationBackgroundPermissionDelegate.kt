package org.noiseplanet.noisecapture.permission.delegate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.permission.LocationManager
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.openNSUrl
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusRestricted

internal class LocationBackgroundPermissionDelegate : PermissionDelegate, KoinComponent {

    // - Properties

    private val locationManager: LocationManager by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val permissionStateFlow: StateFlow<PermissionState> = locationManager
        .authorizationStatusFlow
        .map { status ->
            when (status) {
                kCLAuthorizationStatusDenied,
                kCLAuthorizationStatusRestricted,
                kCLAuthorizationStatusAuthorizedWhenInUse,
                    -> PermissionState.DENIED

                kCLAuthorizationStatusAuthorizedAlways,
                    -> PermissionState.GRANTED

                else -> PermissionState.NOT_DETERMINED
            }
        }.stateInWhileSubscribed(
            scope = scope,
            initialValue = PermissionState.NOT_DETERMINED
        )


    // - Lifecycle

    init {
        locationManager.refreshStatus()
    }


    // - Public functions

    override fun checkPermissionState() {
        locationManager.refreshStatus()
    }

    override fun providePermission() {
        locationManager.requestBackgroundAuthorization()
    }

    override fun openSettingPage() {
        openNSUrl("App-Prefs:Privacy&path=LOCATION")
    }
}
