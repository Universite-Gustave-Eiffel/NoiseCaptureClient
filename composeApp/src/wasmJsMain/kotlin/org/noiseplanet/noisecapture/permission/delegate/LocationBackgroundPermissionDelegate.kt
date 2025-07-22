package org.noiseplanet.noisecapture.permission.delegate

import org.noiseplanet.noisecapture.interop.createGeolocationOptions
import org.noiseplanet.noisecapture.interop.navigator
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.permission.DefaultPermissionDelegate
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.util.injectLogger

internal class LocationBackgroundPermissionDelegate : DefaultPermissionDelegate(
    permission = Permission.LOCATION_BACKGROUND
) {

    // - Properties

    private val logger: Logger by injectLogger()


    // - PermissionDelegate

    override fun providePermission() {
        // Try getting current location, this will trigger the permission popup, if needed.
        navigator?.geolocation?.getCurrentPosition(
            success = { pos ->
                logger.debug("Geolocation ping: ${pos?.coords}")
            },
            error = { err ->
                logger.warning("Geolocation ping failed: ${err.message}")
            },
            options = createGeolocationOptions(enableHighAccuracy = true)
        )
    }
}
