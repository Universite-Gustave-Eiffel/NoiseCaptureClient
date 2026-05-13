package org.noiseplanet.noisecapture.util.geo

import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.json.JsonPrimitive
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.dbAverage
import org.noiseplanet.noisecapture.util.isInVuMeterRange
import org.noiseplanet.noisecapture.util.roundTo
import kotlin.math.min


private typealias LocationSequence = List<Map.Entry<Long, Pair<Double, Double>>>
private typealias LeqsSequence = List<Map.Entry<Long, Double>>

/**
 * Build paths from measurement location and sound level data.
 */
object GeoJsonBuilder {

    // - Constants

    /**
     * Minimum distance (in meters) between two path points. If two points are too close by they
     * will be merged in a single one.
     */
    const val DEFAULT_MIN_DIST_BETWEEN_POINTS: Double = 2.0


    // - Public functions

    /**
     * Turns given LEq and location sequences into a GeoJson representation by mapping each acoustic
     * data points to their closest equivalent in the location track, then calculating the energetic
     * mean for each of those points. Lastly turns the result into a GeoJson [FeatureCollection] of [Point].
     *
     * @param leqSequence All measurement's LEq sequence fragments.
     * @param locationSequence All measurement's location sequence fragments.
     *
     * @return A serializable GeoJson [FeatureCollection].
     */
    fun fromMeasurement(
        leqSequence: List<LeqSequenceFragment>,
        locationSequence: List<LocationSequenceFragment>,
    ): FeatureCollection {
        // Get all coordinates and LAEq values for the given measurement,
        // expected to be sorted in ascending order
        val coords = prepareLocationSequence(locationSequence)
        val laeqs = prepareLeqSequence(leqSequence)

        // If location sequence is empty, return empty feature collection
        if (coords.isEmpty()) {
            return FeatureCollection(features = emptyList())
        }

        // If we only have a single point (user is stationary), calculate average of all LAEq values
        if (coords.size == 1) {
            val coord = coords.first()
            val (lat, lon) = coord.value
            val feature = pointFeatureFromValues(
                lat = lat,
                lon = lon,
                timestamp = coord.key,
                laeq = laeqs.map { (_, value) -> value }.dbAverage().roundTo(1)
            )
            return FeatureCollection(features = listOf(feature))
        }

        val result = mutableListOf<Feature>() // Holds resampled features
        val laeqsForFeature = mutableListOf<Double>() // Holds the LAEqs of the current feature
        var locationsCursor = 0 // Current index in the locations list

        for (i in 0 until laeqs.size) {
            // Get timestamps of previous point and current point
            val currLaeq = laeqs[i]
            val currLoc = coords[locationsCursor]
            val nextLoc = coords.getOrNull(locationsCursor + 1)

            // If the current LAEq record is closer to the next location record than to the current
            // one, create a new feature from current location and associated LAEq records,
            // clear the list and increment location cursor
            if (nextLoc != null && (currLaeq.key - currLoc.key > nextLoc.key - currLaeq.key)) {
                // Otherwise, create a new feature from current location and associated records,
                // clear the list and increment location cursor
                if (laeqsForFeature.isNotEmpty()) {
                    result.add(
                        pointFeatureFromValues(
                            lat = currLoc.value.first,
                            lon = currLoc.value.second,
                            timestamp = currLoc.key,
                            laeq = laeqsForFeature.dbAverage().roundTo(1),
                        )
                    )
                }
                locationsCursor = min(locationsCursor + 1, coords.lastIndex)
                laeqsForFeature.clear()
            }

            // Add current LAEq record to the list
            laeqsForFeature.add(currLaeq.value)
        }

        // Add the last feature with remaining elements in the list
        if (laeqsForFeature.isNotEmpty()) {
            result.add(
                pointFeatureFromValues(
                    lat = coords[locationsCursor].value.first,
                    lon = coords[locationsCursor].value.second,
                    timestamp = coords[locationsCursor].key,
                    laeq = laeqsForFeature.dbAverage().roundTo(1),
                )
            )
        }

        return FeatureCollection(features = result)
    }


    // - Private functions

    /**
     * Creates a GeoJson [Feature] object with [Point] geometry and given `laeq` and `timestamp`
     * properties.
     * Also adds a `marker-color` property with the corresponding noise level palette color.
     *
     * @param lat Latitude
     * @param lon Longitude
     * @param laeq LAEq
     * @param timestamp Timestamp (ms since epoch)
     *
     * @return GeoJson [Feature]
     */
    private fun pointFeatureFromValues(
        lat: Double,
        lon: Double,
        laeq: Double,
        timestamp: Long,
    ): Feature {
        val markerColor = NoiseLevelColorRamp.getColorForSPLValue(laeq).toArgb()
        return Feature(
            geometry = Point(positionOf(lon, lat)),
            properties = mapOf(
                "laeq" to JsonPrimitive(laeq),
                "timestamp" to JsonPrimitive(timestamp),
                // Encode marker color to GeoJson, drop the first two characters
                // corresponding to alpha channel
                "marker-color" to JsonPrimitive("#" + markerColor.toHexString().drop(2))
            )
        )
    }

    /**
     * Concatenates all lat/lon coordinates points found in fragments.
     * Strips out points that too close together.
     *
     * @param locationSequence All measurement's location sequence fragments.
     *
     * @return A list of map entries with timestamp as key and coordinates as value (lat, lon),
     *         sorted in ascending order by timestamp.
     */
    private fun prepareLocationSequence(
        locationSequence: List<LocationSequenceFragment>,
    ): LocationSequence {
        // Match all points with their associated timestamp
        val sortedPoints = locationSequence
            .fold(mapOf<Long, Pair<Double, Double>>()) { accumulator, fragment ->
                val latLonPairs = fragment.lat.zip(fragment.lon)
                val timestampedPoints = fragment.timestamp.zip(latLonPairs)
                accumulator + timestampedPoints
            }
            .entries
            .toList()

        // Will hold a reference to the previous non-filtered point
        var prevPoint: Pair<Double, Double>? = null

        return sortedPoints.filterIndexed { index, entry ->
            if (index == 0) {
                prevPoint = entry.value
                return@filterIndexed true
            }
            val (prevLat, prevLon) = prevPoint ?: return@filterIndexed false
            val (currLat, currLon) = entry.value

            // Compute distance between last and current point
            val dist = GeoUtil.equirectangularDistance(prevLat, prevLon, currLat, currLon) * 1_000.0

            if (dist < DEFAULT_MIN_DIST_BETWEEN_POINTS) {
                // If distance is under threshold, skip this point and move to the next one
                false
            } else {
                // If distance is above threshold, keep this point and use it as previous point for
                // the next calculations
                prevPoint = entry.value
                true
            }
        }
    }

    /**
     * Concatenates all sound level values found in fragments.
     *
     * @param leqSequence Measurement unique identifier
     *
     * @return A list of map entries with timestamp as key and LAEq as value,
     *         sorted in ascending order by timestamp.
     */
    private fun prepareLeqSequence(
        leqSequence: List<LeqSequenceFragment>,
    ): LeqsSequence = leqSequence
        .fold(mapOf<Long, Double>()) { accumulator, fragment ->
            val laeqs = fragment.timestamp.zip(fragment.laeq)
            accumulator + laeqs
        }
        .filter { (_, laeq) ->
            laeq.isInVuMeterRange()
        }
        .entries
        .toList()
}
