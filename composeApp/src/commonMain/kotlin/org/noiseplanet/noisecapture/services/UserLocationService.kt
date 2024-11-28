package org.noiseplanet.noisecapture.services

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.Location
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

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


/**
 * Default [UserLocationService] implementation
 */
class DefaultUserLocationService : UserLocationService, KoinComponent {

    // - Constants

    companion object {

        private const val TAG = "UserLocationService"
    }


    // - Properties

    private val logger: Logger by inject { parametersOf(TAG) }


    // - UserLocationService

    override fun getCurrentLocation(): Location? {
        // TODO: Fetch actual user location
        logger.debug("Get current location")

        return Location(
            lat = Random.nextDouble(),
            lon = Random.nextDouble()
        )
    }

    override fun getLiveLocation(): Flow<Location> {
        // TODO: Subscribe to live user location
        logger.debug("Get live location")

        return flow {
            // For now, just emmit dummy locations once every second
            emit(
                Location(
                    lat = Random.nextDouble(),
                    lon = Random.nextDouble(),
                )
            )
            delay(1.seconds.inWholeMilliseconds)
        }
    }
}
