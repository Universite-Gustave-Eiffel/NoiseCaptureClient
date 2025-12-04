package org.noiseplanet.noisecapture.model.dao

/**
 * Location data at a given time
 *
 * @param timestamp Time at which this point was recorded in milliseconds since epoch UTC.
 * @param lat Geographical latitude given by the device.
 * @param lon Geographical longitude given by the device.
 * @param speed The instantaneous speed of the device, measured in meters per second.
 *              Not always available from browser, hence optional.
 * @param altitude The altitude above mean sea level associated with a location, measured in meters.
 *                 Not always available from browser, hence optional.
 * @param direction Direction in which the device is traveling, measured in degrees and relative to due north.
 *                  Not always available from browser, hence optional.
 * @param horizontalAccuracy The radius of uncertainty for the location, measured in meters.
 * @param verticalAccuracy The validity of the altitude values, and their estimated uncertainty, measured in meters.
 *                         On Android, only available since SDK 26 (Oreo), hence optional.
 * @param speedAccuracy The instantaneous speed of the device, measured in meters per second.
 *                      On Android, only available since SDK 26 (Oreo), hence optional.
 * @param directionAccuracy The accuracy of the direction value, measured in degrees.
 *                          On Android, only available since SDK 26 (Oreo), hence optional.
 */
data class LocationRecord(
    val timestamp: Long,

    val lat: Double,
    val lon: Double,
    val speed: Double?,
    val altitude: Double?,
    val direction: Double?,

    val horizontalAccuracy: Double,
    val verticalAccuracy: Double? = null,
    val speedAccuracy: Double? = null,
    val directionAccuracy: Double? = null,
)
