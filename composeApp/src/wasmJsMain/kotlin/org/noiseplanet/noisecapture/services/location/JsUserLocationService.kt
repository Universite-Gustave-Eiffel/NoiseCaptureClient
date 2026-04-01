package org.noiseplanet.noisecapture.services.location

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


class JsUserLocationService : DefaultUserLocationService() {

    // - Properties

    /**
     * In Js targets, there is no location foreground permission, and no location services on/off
     * either. So we rely solely on location background permission state.
     */
    override val isLocationAvailable: StateFlow<Boolean> = permissionService
        .getPermissionStateFlow(Permission.LOCATION_BACKGROUND)
        .map { it == PermissionState.GRANTED }
        .stateInWhileSubscribed(scope = scope, initialValue = false)
}
