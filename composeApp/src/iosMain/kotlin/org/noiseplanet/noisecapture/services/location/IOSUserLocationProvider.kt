package org.noiseplanet.noisecapture.services.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.LocationRecord
import org.noiseplanet.noisecapture.util.injectLogger
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationAccuracy
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBestForNavigation
import platform.Foundation.NSError
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject

/**
 * IOS implementation of location provider
 */
@OptIn(ExperimentalForeignApi::class)
class IOSUserLocationProvider : UserLocationProvider, KoinComponent {

    // - Constants

    companion object {

        private val DESIRED_ACCURACY: CLLocationAccuracy = kCLLocationAccuracyBestForNavigation
    }


    // - Properties

    private val locationManager by lazy {
        // Initialize this property lazily so we can create it from the desired thread.
        CLLocationManager()
    }
    private val locationFlow = MutableSharedFlow<LocationRecord>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val logger: Logger by injectLogger()

    private val delegate = CLLocationProviderDelegate(logger, ::onNewLocationOrHeadingData)
    private val mainScope = CoroutineScope(Dispatchers.Main)


    // - Lifecycle

    init {
        // CLLocationManager must be initialized on a thread with an active run loop, like the
        // application main thread. Calls are still asynchronous so it should not mess with the
        // UI responsiveness.
        // See: https://developer.apple.com/documentation/corelocation/cllocationmanagerdelegate
        mainScope.launch {
            // Set desired accuracy and attach delegate to be notified of location updates
            locationManager.desiredAccuracy = DESIRED_ACCURACY
            locationManager.delegate = delegate
        }
    }


    // - UserLocationProvider

    override val currentLocation: LocationRecord?
        get() = locationFlow.replayCache.firstOrNull()

    override val liveLocation: Flow<LocationRecord>
        get() = locationFlow.asSharedFlow()

    override fun startUpdatingLocation() {
        locationManager.startUpdatingLocation()
        locationManager.startUpdatingHeading()
    }

    override fun stopUpdatingLocation() {
        locationManager.stopUpdatingLocation()
        locationManager.stopUpdatingHeading()
    }


    // - Private functions

    /**
     * Called when either location or heading gets a new recorded value.
     * If only one value is provided, this method will try to fetch the latest location or heading
     * value available from the location manager.
     */
    private fun onNewLocationOrHeadingData(newLocation: CLLocation?, newHeading: CLHeading?) {
        // Get the latest available location and heading values
        val rawLocation: CLLocation = newLocation ?: locationManager.location ?: return
        val rawHeading: CLHeading? = newHeading ?: locationManager.heading

        // Create Location object from raw values
        rawLocation.coordinate.useContents {
            // Our timestamp value for this point will be the latest value between heading
            // and location objects
            val timestamp = maxOf(
                a = rawLocation.timestamp.timeIntervalSince1970,
                b = rawHeading?.timestamp?.timeIntervalSince1970 ?: 0.0,
            ).toLong()

            val location = LocationRecord(
                timestamp = timestamp,
                lat = latitude,
                lon = longitude,
                speed = rawLocation.speed,
                altitude = rawLocation.altitude,
                direction = rawLocation.course,
                orientation = rawHeading?.trueHeading,
                horizontalAccuracy = rawLocation.horizontalAccuracy,
                verticalAccuracy = rawLocation.verticalAccuracy,
                speedAccuracy = rawLocation.speedAccuracy,
                directionAccuracy = rawLocation.courseAccuracy,
                orientationAccuracy = rawHeading?.headingAccuracy,
            )

            // Emit new location data through mutable shared flow
            locationFlow.tryEmit(location)
        }
    }
}


/**
 * [CLLocationManagerDelegateProtocol] implementation to subscribe to location updates
 */
private class CLLocationProviderDelegate(
    private val logger: Logger,
    private val didReceiveLocationOrHeadingUpdate: (CLLocation?, CLHeading?) -> Unit,
) : NSObject(), CLLocationManagerDelegateProtocol {

    // - CLLocationManagerDelegateProtocol

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        if (didUpdateLocations.isEmpty()) {
            logger.debug("Location history was empty")
            return
        }

        val lastLocation = didUpdateLocations.last() as? CLLocation
        if (lastLocation == null) {
            logger.warning("Could not properly cast CLLocation object")
            return
        }

        didReceiveLocationOrHeadingUpdate(lastLocation, null)
    }

    override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
        didReceiveLocationOrHeadingUpdate(null, didUpdateHeading)
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        logger.warning("Failed to retrieve location: ${didFailWithError.localizedDescription}")
    }
}
