package org.noiseplanet.noisecapture.permission.delegate

import org.koin.core.logger.Logger
import org.koin.mp.KoinPlatformTools
import org.noiseplanet.noisecapture.interop.createGeolocationOptions
import org.noiseplanet.noisecapture.interop.navigator
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.checkPermission

internal class LocationBackgroundPermissionDelegate(
    private val logger: Logger = KoinPlatformTools.defaultLogger(),
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
                logger.warn("Geolocation ping failed: ${err.message}")
                permissionSate = PermissionState.DENIED
            },
            options = createGeolocationOptions(enableHighAccuracy = true)
        )
    }

    override fun openSettingPage() {
        // TODO: Show popup?
    }
}
