package org.noiseplanet.noisecapture.permission.delegate

import org.noiseplanet.noisecapture.interop.createGeolocationOptions
import org.noiseplanet.noisecapture.interop.navigator
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.checkPermission

internal class LocationBackgroundPermissionDelegate(
    private val logger: Logger,
) : PermissionDelegate {

    private var permissionSate = PermissionState.NOT_DETERMINED

    override suspend fun getPermissionState(): PermissionState {
        if (permissionSate != PermissionState.NOT_DETERMINED) {
            return permissionSate
        }
        return checkPermission(Permission.LOCATION_BACKGROUND)
    }

    override suspend fun providePermission() {
        navigator?.geolocation?.getCurrentPosition(
            success = { pos ->
                logger.debug("Geolocation ping: ${pos?.coords}")
                permissionSate = PermissionState.GRANTED
            },
            error = { err ->
                logger.warning("Geolocation ping failed: ${err.message}")
                permissionSate = PermissionState.DENIED
            },
            options = createGeolocationOptions(enableHighAccuracy = true)
        )
    }

    override fun openSettingPage() {
        // TODO: Show popup?
    }
}
