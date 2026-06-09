package org.noiseplanet.noisecapture.services.location

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.LocationRecord
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import org.noiseplanet.noisecapture.services.settings.UserSettingsService
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


/**
 * Default [UserLocationService] implementation
 */
@OptIn(ExperimentalCoroutinesApi::class)
open class DefaultUserLocationService : UserLocationService, KoinComponent {

    // - Properties

    private val locationProvider: UserLocationProvider by inject()
    private val settingsService: UserSettingsService by inject()

    protected val permissionService: PermissionService by inject()
    protected val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())


    // - UserLocationService

    override val currentLocation: LocationRecord?
        get() = locationProvider.currentLocation

    override val liveLocation: Flow<LocationRecord> = locationProvider.liveLocation

    override val isLocationAvailable: StateFlow<Boolean> = permissionService
        .getPermissionStateFlow(Permission.LOCATION_SERVICE_ON)
        .combine(permissionService.getPermissionStateFlow(Permission.LOCATION)) { services, foreground ->
            services == PermissionState.GRANTED && foreground == PermissionState.GRANTED
        }
        .stateInWhileSubscribed(scope = scope, initialValue = false)

    override val isSignalPoor: StateFlow<Boolean> = locationProvider.liveLocation
        .map { it.horizontalAccuracy > UserLocationService.LOCATION_ACCURACY_POOR_THRESHOLD }
        .stateInWhileSubscribed(scope = scope, initialValue = false)

    override fun startUpdatingLocation() {
        locationProvider.startUpdatingLocation()
    }

    override fun stopUpdatingLocation() {
        locationProvider.stopUpdatingLocation()
    }


    // - Lifecycle

    init {
        scope.launch {
            liveLocation.collect {
                // Listen to live location updates and store the last known user location
                // to use as default map centroid
                settingsService.set(SettingsKey.MapLastKnownLocation, it)
            }
        }
    }
}
