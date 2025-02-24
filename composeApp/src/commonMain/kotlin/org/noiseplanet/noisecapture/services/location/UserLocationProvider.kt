package org.noiseplanet.noisecapture.services.location

import kotlinx.coroutines.flow.Flow
import org.noiseplanet.noisecapture.model.Location

/**
 * Common interface for user location provider, each platform should provide its own implementation
 */
interface UserLocationProvider {

    /**
     * Gets current user location, if known
     */
    val currentLocation: Location?

    /**
     * Tracks user location updates
     */
    val liveLocation: Flow<Location>

    /**
     * Starts monitoring location updates
     */
    fun startUpdatingLocation()

    /**
     * Stops monitoring location updates
     */
    fun stopUpdatingLocation()
}
