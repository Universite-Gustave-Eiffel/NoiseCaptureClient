package org.noiseplanet.noisecapture.services.location

import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.Location


/**
 * Default [UserLocationService] implementation
 */
class DefaultUserLocationService : UserLocationService, KoinComponent {

    // - Properties

    private val locationProvider: UserLocationProvider by inject()


    // - UserLocationService

    override val currentLocation: Location?
        get() = locationProvider.currentLocation

    override val liveLocation: Flow<Location>
        get() = locationProvider.liveLocation

    override fun startUpdatingLocation() {
        locationProvider.startUpdatingLocation()
    }

    override fun stopUpdatingLocation() {
        locationProvider.stopUpdatingLocation()
    }
}
