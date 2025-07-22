package org.noiseplanet.noisecapture.permission.delegate

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.permission.LocationManager
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.openNSUrl

internal class LocationServicePermissionDelegate : PermissionDelegate, KoinComponent {

    // - Properties

    private val locationManager: LocationManager by inject()

    private val permissionMutableSateFlow = MutableStateFlow(PermissionState.NOT_DETERMINED)
    override val permissionStateFlow: StateFlow<PermissionState> = permissionMutableSateFlow


    // - Lifecycle

    init {
        checkPermissionState()
    }


    // - Public functions

    override fun checkPermissionState() {
        val state = if (locationManager.locationServicesEnabled) {
            PermissionState.GRANTED
        } else {
            PermissionState.DENIED
        }
        permissionMutableSateFlow.tryEmit(state)
    }

    override fun providePermission() {
        openSettingPage()
    }

    override fun canOpenSettings(): Boolean = true

    override fun openSettingPage() {
        openNSUrl("App-Prefs:Privacy&path=LOCATION")
    }
}
