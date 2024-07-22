package org.noiseplanet.noisecapture.permission.delegate

import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.util.checkPermission

internal class LocationBackgroundPermissionDelegate : PermissionDelegate {

    private var permissionSate = PermissionState.NOT_DETERMINED

    override suspend fun getPermissionState(): PermissionState {
        if (permissionSate == PermissionState.GRANTED) {
            return permissionSate
        }
        return checkPermission(Permission.LOCATION_BACKGROUND)
    }

    override suspend fun providePermission() {
        navigator?.geolocation?.getCurrentPosition(
            success = { pos ->
                println(pos)
                permissionSate = PermissionState.GRANTED
            },
            error = { err ->
                println(err)
                permissionSate = PermissionState.DENIED
            },
            options = createGeolocationOptions(enableHighAccuracy = true)
        )
    }

    override fun openSettingPage() {
        // TODO: Show popup?
    }
}


// TODO: Move all the code below to their separate files

external val navigator: Navigator?

external class Navigator {

    val geolocation: Geolocation?
}

/**
 * The GeolocationPosition interface represents the position of the concerned device at a given
 * time. The position, represented by a GeolocationCoordinates object, comprehends the 2D position
 * of the device, on a spheroid representing the Earth, but also its altitude and its speed.
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/GeolocationPosition)
 *
 * @property coords The [GeolocationCoordinates] object containing the device's current location.
 * @property timestamp The time at which the location was retrieved.
 */
external class GeolocationPosition {

    val coords: GeolocationCoordinates

    val timestamp: Double
}

/**
 * The GeolocationCoordinates interface represents the position and altitude of the device on Earth,
 * as well as the accuracy with which these properties are calculated. The geographic position
 * information is provided in terms of World Geodetic System coordinates (WGS84).
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/GeolocationCoordinates).
 *
 * @property latitude Returns a double representing the position's latitude in decimal degrees.
 * @property longitude Returns a double representing the position's longitude in decimal degrees.
 * @property accuracy Returns a double representing the accuracy of the latitude and longitude
 * properties, expressed in meters.
 * @property altitude Returns a double representing the position's altitude in meters, relative to
 * nominal sea level. This value can be null if the implementation cannot provide the data.
 * @property altitudeAccuracy Returns a double representing the accuracy of the altitude expressed
 * in meters. This value can be null if the implementation cannot provide the data.
 * @property heading Returns a double representing the direction towards which the device is facing.
 * This value, specified in degrees, indicates how far off from heading true north the device is.
 * 0 degrees represents true north, and the direction is determined clockwise (which means that east
 * is 90 degrees and west is 270 degrees). If speed is 0, heading is NaN. If the device is unable to
 * provide heading information, this value is null.
 * @property speed Returns a double representing the velocity of the device in meters per second.
 * This value can be null.
 */
external class GeolocationCoordinates {

    val latitude: Double
    val longitude: Double
    val accuracy: Double
    val altitude: Double?
    val altitudeAccuracy: Double?
    val heading: Double?
    val speed: Double?
}


@Suppress("UnusedParameter")
fun createGeolocationOptions(
    enableHighAccuracy: Boolean = false,
    timeout: Double = Double.POSITIVE_INFINITY,
    maximumAge: Double = 0.0,
): JsAny =
    js("({enableHighAccuracy: enableHighAccuracy, timeout: timeout, maximumAge: maximumAge})")


/**
 * Represents an error that occurred while retrieving the geolocation.
 *
 * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/GeolocationPositionError)
 *
 * @property code The error code.
 * @property message A human-readable message describing the error.
 */
public external class GeolocationPositionError {

    /**
     * The error code.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/GeolocationPositionError/code)
     *
     * The following values are supported:
     * - 1: Permission denied
     * - 2: Position unavailable
     * - 3: Timeout
     */
    public var code: Int

    /**
     * A human-readable message describing the error.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/GeolocationPositionError/message)
     */
    public var message: String
}

internal enum class GeolocationPositionErrorCode(val code: Int) {
    PermissionDenied(1),
    PositionUnavailable(2),
    Timeout(3)
}

internal fun GeolocationPositionError.value(): GeolocationPositionErrorCode =
    GeolocationPositionErrorCode.entries.first { it.code == code }

/**
 * GeoLocation API for accessing the browser's location data.
 *
 * See [Geolocation](https://developer.mozilla.org/en-US/docs/Web/API/Geolocation)
 */
public external class Geolocation {

    /**
     * Returns the current position of the device.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/Geolocation/getCurrentPosition).
     *
     * @param success Callback function that is called with the current position.
     * @param error Callback function that is called when an error occurs.
     * @param options An optional object that provides options for the request.
     */
    public fun getCurrentPosition(
        success: (GeolocationPosition?) -> Unit,
        error: (GeolocationPositionError) -> Unit,
        options: JsAny,
    )

    /**
     * Starts watching the position of the device.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/Geolocation/watchPosition).
     *
     * @param success Callback function that is called with the current position.
     * @param error Callback function that is called when an error occurs.
     * @param options An optional object that provides options for the request.
     * @return A watch ID that can be used to clear the watch.
     */
    public fun watchPosition(
        success: (GeolocationPosition?) -> Unit,
        error: (GeolocationPositionError) -> Unit,
        options: JsAny,
    ): Int

    /**
     * Clear an existing watch operation.
     *
     * [MDN Reference](https://developer.mozilla.org/en-US/docs/Web/API/Geolocation/clearWatch).
     *
     * @param watchId The ID of the watch operation to clear.
     */
    public fun clearWatch(watchId: Int)
}
