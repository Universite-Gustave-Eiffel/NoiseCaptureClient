package org.noiseplanet.noisecapture.services.location

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.interop.GeolocationPosition
import org.noiseplanet.noisecapture.interop.createGeolocationOptions
import org.noiseplanet.noisecapture.interop.navigator
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.LocationRecord
import org.noiseplanet.noisecapture.util.injectLogger

class WasmJSUserLocationProvider : KoinComponent, UserLocationProvider {

    // - Constants

    companion object {

        private const val ENABLE_HIGH_ACCURACY: Boolean = true
    }


    // - Properties

    private val logger: Logger by injectLogger()

    private val locationsFlow = MutableSharedFlow<LocationRecord>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Holds the ID returned by `watchPosition`
     */
    private var taskId: Int? = null


    // - UserLocationProvider

    override val currentLocation: LocationRecord?
        get() = locationsFlow.replayCache.firstOrNull()

    override val liveLocation: Flow<LocationRecord>
        get() = locationsFlow.asSharedFlow()

    override fun startUpdatingLocation() {
        taskId = navigator?.geolocation?.watchPosition(
            success = { pos ->
                pos?.let {
                    val location = buildLocationFromRawData(it)
                    locationsFlow.tryEmit(location)
                }
            },
            error = { err ->
                logger.warning("Unable to retrieve position: ${err.message}")
            },
            options = createGeolocationOptions(
                enableHighAccuracy = ENABLE_HIGH_ACCURACY,
            )
        )
    }

    override fun stopUpdatingLocation() {
        taskId?.let {
            navigator?.geolocation?.clearWatch(it)
        }
        taskId = null
    }


    // - Private functions

    /**
     * Parses the raw geolocation data to a [LocationRecord] object.
     *
     * @param rawLocation Raw geolocation data.
     *
     * @return [LocationRecord] instance from raw data.
     */
    private fun buildLocationFromRawData(rawLocation: GeolocationPosition): LocationRecord {
        return LocationRecord(
            timestamp = rawLocation.timestamp.toLong(),
            lat = rawLocation.coords.latitude,
            lon = rawLocation.coords.longitude,
            altitude = rawLocation.coords.altitude,
            speed = rawLocation.coords.speed,
            // For JS, direction and orientation are represented by the same property
            // https://developer.mozilla.org/en-US/docs/Web/API/GeolocationCoordinates/heading
            direction = rawLocation.coords.heading,
            orientation = rawLocation.coords.heading,
            horizontalAccuracy = rawLocation.coords.accuracy,
            verticalAccuracy = rawLocation.coords.altitudeAccuracy,
        )
    }
}
