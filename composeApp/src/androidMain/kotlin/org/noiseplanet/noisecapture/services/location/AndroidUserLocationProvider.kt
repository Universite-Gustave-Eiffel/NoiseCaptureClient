package org.noiseplanet.noisecapture.services.location

import android.os.Build
import com.google.android.gms.location.DeviceOrientation
import com.google.android.gms.location.DeviceOrientationListener
import com.google.android.gms.location.DeviceOrientationRequest
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.Coordinates
import org.noiseplanet.noisecapture.model.LocationAccuracy
import org.noiseplanet.noisecapture.model.LocationRecord
import org.noiseplanet.noisecapture.util.injectLogger
import org.noiseplanet.noisecapture.util.throttleLatest
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


private typealias RawLocation = android.location.Location

class AndroidUserLocationProvider :
    UserLocationProvider,
    KoinComponent,
    LocationListener,
    DeviceOrientationListener {

    // - Constants

    companion object {

        private const val LOCATION_REQUEST_PRIORITY: Int = Priority.PRIORITY_BALANCED_POWER_ACCURACY
        private const val LOCATION_REQUEST_INTERVAL_SECONDS: Long = 3
    }


    // - Properties

    private val logger: Logger by injectLogger()

    private val rawLocationFlow = MutableSharedFlow<RawLocation>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val rawOrientationFlow = MutableSharedFlow<DeviceOrientation>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Specifies the desired accuracy/power policy and interval between location updates
     */
    private val locationRequest by lazy {
        LocationRequest.Builder(
            LOCATION_REQUEST_PRIORITY,
            TimeUnit.SECONDS.toMillis(LOCATION_REQUEST_INTERVAL_SECONDS),
        ).build()
    }
    private val locationClient = LocationServices.getFusedLocationProviderClient(get())

    /**
     * Specifies the desired accuracy/power policy and interval between orientation updates
     */
    private val orientationRequest by lazy {
        DeviceOrientationRequest.Builder(
            DeviceOrientationRequest.OUTPUT_PERIOD_DEFAULT
        ).build()
    }
    private val orientationClient = LocationServices.getFusedOrientationProviderClient(get())

    /**
     * Executor on which location and orientation requests will run
     */
    private val executor by lazy { Executors.newSingleThreadScheduledExecutor() }


    // - UserLocationProvider

    override val liveLocation: Flow<LocationRecord>
        get() = rawOrientationFlow
            // Device orientation is given every 20ms minimum, so we throttle the values to
            // match location updates frequency
            .throttleLatest(TimeUnit.SECONDS.toMillis(LOCATION_REQUEST_INTERVAL_SECONDS))
            .zip(rawLocationFlow) { orientation, location ->
                // Zip the results of both flows to get location updates with both
                // position and orientation.
                logger.debug("Got new orientation and location: $orientation, $location")
                buildLocationFromRawData(location, orientation)
            }

    override val currentLocation: LocationRecord?
        get() = runBlocking { liveLocation.lastOrNull() }

    override fun startUpdatingLocation() {
        try {
            // Start both location and orientation updates
            locationClient.requestLocationUpdates(
                locationRequest,
                executor,
                this,
            )
            orientationClient.requestOrientationUpdates(
                orientationRequest,
                executor,
                this,
            )
        } catch (exception: SecurityException) {
            logger.error("The required location permissions were not granted.", exception)
        }
    }

    override fun stopUpdatingLocation() {
        locationClient.removeLocationUpdates(this)
        orientationClient.removeOrientationUpdates(this)
    }


    // - LocationListener

    override fun onLocationChanged(rawLocation: RawLocation) {
        rawLocationFlow.tryEmit(rawLocation)
    }


    // - DeviceOrientationListener

    override fun onDeviceOrientationChanged(rawOrientation: DeviceOrientation) {
        rawOrientationFlow.tryEmit(rawOrientation)
    }


    // - Private functions

    /**
     * Converts the given raw location and orientation data to a [LocationRecord] instance.
     *
     * @param rawLocation Raw [android.location.Location] object.
     * @param rawOrientation Raw [DeviceOrientation] object.
     */
    private fun buildLocationFromRawData(
        rawLocation: RawLocation,
        rawOrientation: DeviceOrientation,
    ): LocationRecord {
        val coordinates = Coordinates(
            lon = rawLocation.longitude,
            lat = rawLocation.latitude
        )

        val accuracy = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocationAccuracy(
                horizontal = rawLocation.accuracy.toDouble(),
                orientation = rawOrientation.headingErrorDegrees.toDouble(),
                // Those parameters are only available since SDK 26 (Android O)
                speed = rawLocation.speedAccuracyMetersPerSecond.toDouble(),
                vertical = rawLocation.verticalAccuracyMeters.toDouble(),
                direction = rawLocation.bearingAccuracyDegrees.toDouble(),
            )
        } else {
            LocationAccuracy(
                horizontal = rawLocation.accuracy.toDouble(),
                orientation = rawOrientation.headingDegrees.toDouble(),
            )
        }

        return LocationRecord(
            timestamp = rawLocation.time.toDouble(),
            coordinates = coordinates,
            altitude = rawLocation.altitude,
            speed = rawLocation.speed.toDouble(),
            direction = rawLocation.bearing.toDouble(),
            orientation = rawOrientation.headingDegrees.toDouble(),
            accuracy = accuracy,
        )
    }
}
