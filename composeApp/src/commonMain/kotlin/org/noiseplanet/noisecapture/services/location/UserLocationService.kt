package org.noiseplanet.noisecapture.services.location

import kotlinx.coroutines.flow.Flow
import org.noiseplanet.noisecapture.model.Location

/**
 * Get user location updates
 */
interface UserLocationService {

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
