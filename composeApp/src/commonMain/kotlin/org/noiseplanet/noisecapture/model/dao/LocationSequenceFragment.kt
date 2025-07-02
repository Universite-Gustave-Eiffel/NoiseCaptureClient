package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * For compression reasons when storing values in JSON format, we want to group values
 * into a representation where each property is an array of values to spare writing the
 * keys everytime.
 *
 * TODO: Figure out a neat way to expose immutable versions of the lists using a backing
 *       property or something, and only leave a public method to push a new record to
 *       the sequence fragment.
 *       Look into explicit backing fields introduced in Kotlin 2.0 when K2 mode will be supported
 *       by the compose multiplatform plugin.
 *       https://github.com/Kotlin/KEEP/blob/explicit-backing-fields-re/proposals/explicit-backing-fields.md
 */
@Serializable
@OptIn(ExperimentalUuidApi::class)
data class LocationSequenceFragment(
    override val uuid: String = Uuid.random().toString(),
    override val index: Int,
    override val measurementId: String,
    override val timestamp: MutableList<Long> = mutableListOf(),

    val lat: MutableList<Double> = mutableListOf(),
    val lon: MutableList<Double> = mutableListOf(),
    val speed: MutableList<Double?> = mutableListOf(),
    val altitude: MutableList<Double?> = mutableListOf(),
    val direction: MutableList<Double?> = mutableListOf(),
    val orientation: MutableList<Double?> = mutableListOf(),

    val horizontalAccuracy: MutableList<Double> = mutableListOf(),
    val verticalAccuracy: MutableList<Double?> = mutableListOf(),
    val speedAccuracy: MutableList<Double?> = mutableListOf(),
    val directionAccuracy: MutableList<Double?> = mutableListOf(),
    val orientationAccuracy: MutableList<Double?> = mutableListOf(),
) : SequenceFragment<LocationRecord> {

    override fun push(element: LocationRecord) {
        timestamp.add(element.timestamp)
        lat.add(element.lat)
        lon.add(element.lon)
        speed.add(element.speed)
        altitude.add(element.altitude)
        direction.add(element.direction)
        orientation.add(element.orientation)
        horizontalAccuracy.add(element.horizontalAccuracy)
        verticalAccuracy.add(element.verticalAccuracy)
        speedAccuracy.add(element.speedAccuracy)
        directionAccuracy.add(element.directionAccuracy)
        orientationAccuracy.add(element.orientationAccuracy)
    }
}
