package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable

/**
 * Location data at a given time
 *
 * @param timestamp     Time at which this point was recorded.
 * @param coordinates   Geographical coordinates of the device.
 * @param speed         The instantaneous speed of the device, measured in meters per second.
 *                      Not always available from browser, hence optional.
 * @param altitude      The altitude above mean sea level associated with a location, measured in meters.
 *                      Not always available from browser, hence optional.
 * @param direction     Direction in which the device is traveling, measured in degrees and relative to due north.
 *                      Not always available from browser, hence optional.
 * @param orientation   Direction in which the device is facing, measured in degrees and relative to true north.
 *                      May not always be available, hence optional.
 * @param accuracy      Accuracy values associated with each of the measured values. See [LocationAccuracy].
 */
@Serializable
data class LocationRecord(
    val timestamp: Double,
    val coordinates: Coordinates,
    val speed: Double?,
    val altitude: Double?,
    val direction: Double?,
    val orientation: Double?,
    val accuracy: LocationAccuracy,
)


/**
 * Accuracy of a location data point.
 *
 * @param horizontal    The radius of uncertainty for the location, measured in meters.
 * @param vertical      The validity of the altitude values, and their estimated uncertainty, measured in meters.
 *                      On Android, only available since SDK 26 (Oreo), hence optional.
 * @param speed         The instantaneous speed of the device, measured in meters per second.
 *                      On Android, only available since SDK 26 (Oreo), hence optional.
 * @param direction     The accuracy of the direction value, measured in degrees.
 *                      On Android, only available since SDK 26 (Oreo), hence optional.
 * @param orientation   The maximum deviation (measured in degrees) between the reported heading and
 *                      the true geomagnetic heading. May not always be available, hence optional.
 */
@Serializable
data class LocationAccuracy(
    val horizontal: Double,
    val speed: Double? = null,
    val vertical: Double? = null,
    val direction: Double? = null,
    val orientation: Double? = null,
)


/**
 * A point with latitude and longitude values
 */
@Serializable
data class Coordinates(
    val lat: Double,
    val lon: Double,
)
