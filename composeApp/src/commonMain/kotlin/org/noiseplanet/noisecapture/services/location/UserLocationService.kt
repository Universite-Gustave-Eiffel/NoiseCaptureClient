package org.noiseplanet.noisecapture.services.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.noiseplanet.noisecapture.model.dao.LocationRecord

/**
 * Get user location updates
 */
interface UserLocationService {

    /**
     * Gets current user location, if known
     */
    val currentLocation: LocationRecord?

    /**
     * Tracks user location updates
     */
    val liveLocation: Flow<LocationRecord>

    /**
     * Current location permission state (combines location services on/off + location
     * foreground permissions)
     */
    val isLocationAvailable: StateFlow<Boolean>

    /**
     * Starts monitoring location updates
     */
    fun startUpdatingLocation()

    /**
     * Stops monitoring location updates
     */
    fun stopUpdatingLocation()
}
