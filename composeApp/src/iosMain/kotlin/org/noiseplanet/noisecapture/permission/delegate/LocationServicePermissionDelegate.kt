package org.noiseplanet.noisecapture.permission.delegate

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.permission.LocationManager
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.openURL
import org.noiseplanet.noisecapture.util.injectLogger
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

internal class LocationServicePermissionDelegate : PermissionDelegate, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()

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
        UIApplication.sharedApplication.openURL(UIApplicationOpenSettingsURLString) { _, error ->
            error?.let { logger.error(it) }
        }
    }
}
