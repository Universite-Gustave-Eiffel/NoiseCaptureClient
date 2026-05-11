package org.noiseplanet.noisecapture.util.geo

import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.util.dbAverage
import org.noiseplanet.noisecapture.util.isInVuMeterRange
import org.noiseplanet.noisecapture.util.roundTo


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
     * For a measurement with the given identifier, retrieves both location and LAEq values and
     * returns a list of points with lat/lon coordinates, a timestamp in milliseconds since epoch,
     * and an associated LAEq value that corresponds to the energetic average of all levels between
     * the last point and this one.
     *
     * > TODO: Test performance on larger measurements with lots of points. To avoid recalculating
     *         averages everytime, we could store the obtained mean value directly in the location
     *         sequence as an optional property?
     *
     * @param leqSequence All measurement's LEq sequence fragments.
     * @param locationSequence All measurement's location sequence fragments.
     *
     * @return A list of [PathPoint].
     */
    fun pathForMeasurement(
        leqSequence: List<LeqSequenceFragment>,
        locationSequence: List<LocationSequenceFragment>,
    ): List<PathPoint> {
        // Get all coordinates and LAEq values for the given measurement,
        // expected to be sorted in ascending order
        val coords = prepareLocationSequence(locationSequence)
        val laeqs = prepareLeqSequence(leqSequence)

        // If we only have a single point (user is stationary), calculate average of all LAEq values
        if (coords.size == 1) {
            val coord = coords.first()
            val (lat, lon) = coord.value

            val point = PathPoint(
                timestamp = coord.key,
                latitude = lat,
                longitude = lon,
                level = laeqs.map { (_, value) -> value }.dbAverage().roundTo(1)
            )
            return listOf(point)
        }

        val result = mutableListOf<PathPoint>() // Will hold resampled data points
        var laeqsCursor = 0 // Current index in the sound levels list

        for (i in 1 until coords.size) {
            // Get timestamps of previous point and current point
            val prevTime = coords[i - 1].key
            val currTime = coords[i].key
            val laeqsForTimeWindow = mutableListOf<Double>()

            // Process all sound entries in [prevTime, currTime)
            while (laeqsCursor < laeqs.size && laeqs[laeqsCursor].key < currTime) {
                val laeqEntry = laeqs[laeqsCursor]
                if (laeqEntry.key >= prevTime) {
                    laeqsForTimeWindow.add(laeqEntry.value)
                }
                laeqsCursor++
            }

            // Calculate energetic mean and push new point to path data
            if (laeqsForTimeWindow.isNotEmpty()) {
                val point = PathPoint(
                    timestamp = currTime,
                    latitude = coords[i].value.first,
                    longitude = coords[i].value.second,
                    level = laeqsForTimeWindow.dbAverage().roundTo(1),
                )
                result.add(point)
            }
        }
        return result
    }


    // - Private functions

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
    ): List<Map.Entry<Long, Pair<Double, Double>>> {
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
    ): List<Map.Entry<Long, Double>> = leqSequence
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

/**
 * A point of a sound level path.
 *
 * @param timestamp Timestamp in milliseconds since epoch
 * @param latitude Latitude (WGS:84)
 * @param longitude Longitude (WGS:84)
 * @param level Average LAEq from last the path point to this one.
 */
data class PathPoint(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val level: Double,
)
