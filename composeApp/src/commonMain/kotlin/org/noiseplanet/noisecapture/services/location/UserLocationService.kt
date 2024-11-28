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
    fun getCurrentLocation(): Location?

    /**
     * Tracks user location updates
     */
    fun getLiveLocation(): Flow<Location>
}
