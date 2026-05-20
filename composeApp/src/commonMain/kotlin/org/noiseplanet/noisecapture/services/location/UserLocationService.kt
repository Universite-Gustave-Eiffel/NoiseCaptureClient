package org.noiseplanet.noisecapture.services.location

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.noiseplanet.noisecapture.model.dao.LocationRecord

/**
 * Get user location updates
 */
interface UserLocationService {

    // - Constants

    companion object {

        // Accuracy threshold (in meters) above which GPS signal is considered to be poor
        const val LOCATION_ACCURACY_POOR_THRESHOLD: Double = 15.0
    }


    // - Properties

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
     * True if location horizontal accuracy is above threshold.
     * If no location data is available, defaults to false.
     */
    val isSignalPoor: StateFlow<Boolean>


    // - Public functions

    /**
     * Starts monitoring location updates
     */
    fun startUpdatingLocation()

    /**
     * Stops monitoring location updates
     */
    fun stopUpdatingLocation()
}
