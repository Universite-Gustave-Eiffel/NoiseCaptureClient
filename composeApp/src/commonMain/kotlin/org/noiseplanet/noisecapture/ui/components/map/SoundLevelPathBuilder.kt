package org.noiseplanet.noisecapture.ui.components.map

import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.util.GeoUtil
import org.noiseplanet.noisecapture.util.dbAverage
import org.noiseplanet.noisecapture.util.isInVuMeterRange


/**
 * Build paths from measurement location and sound level data.
 */
class SoundLevelPathBuilder(
    private val measurementService: MeasurementService,
) {
    // - Public functions

    /**
     * For a measurement with the given identifier, retrieves both location and LAEq values and
     * returns a list of points with lat/lon coordinates, a timestamp in milliseconds since epoch,
     * and an associated LAEq value that corresponds to the energetic average of all levels between
     * the last point and this one.
     *
     * This is a suspend function because reading measurement data from disk must be done from
     * a coroutine scope.
     *
     * > TODO: Test performance on larger measurements with lots of points. To avoid recalculating
     *         averages everytime, we could store the obtained mean value directly in the location
     *         sequence as an optional property?
     *
     * @param measurementUuid Unique measurement identifier.
     * @return A list of [PathPoint].
     */
    suspend fun pathForMeasurement(measurementUuid: String): List<PathPoint> {
        // Get all coordinates and LAEq values for the given measurement,
        // expected to be sorted in ascending order
        val coords = getSortedLocationSequence(measurementUuid)
        val laeqs = getSortedLaeqSequence(measurementUuid)

        // If we only have a single point (user is stationary), calculate average of all LAEq values,
        // and return it twice so the path still has first and end points.
        if (coords.size == 1) {
            val coord = coords.first()
            val (lat, lon) = coord.value

            val point = PathPoint(
                timestamp = coord.key,
                latitude = lat,
                longitude = lon,
                level = laeqs.map { (_, value) -> value }.dbAverage()
            )
            return listOf(point, point)
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
                    level = laeqsForTimeWindow.dbAverage(),
                )
                result.add(point)
            }
        }
        return result
    }


    // - Private functions

    /**
     * Reads an concatenates all lat/lon coordinates points tied to the measurement with
     * the given unique identifier
     *
     * @param measurementUuid Measurement unique identifier
     * @return A list of map entries with timestamp as key and coordinates as value (lat, lon),
     *         sorted in ascending order by timestamp.
     */
    private suspend fun getSortedLocationSequence(
        measurementUuid: String,
    ): List<Map.Entry<Long, Pair<Double, Double>>> {
        // Get all points tied to the measurement
        val sortedPoints = measurementService.getLocationSequenceForMeasurement(measurementUuid)
            .fold(mapOf<Long, Pair<Double, Double>>()) { accumulator, fragment ->
                val latLonPairs = fragment.lat.zip(fragment.lon)
                val timestampedPoints = fragment.timestamp.zip(latLonPairs)
                accumulator + timestampedPoints
            }
            .entries
            .toList()

        // Will hold a reference to the previous non-filtered point
        var prevPoint: Pair<Double, Double>? = null

        // Minimum distance required between two points, in meters
        val distThreshold = 2.0

        val dbg = sortedPoints.filterIndexed { index, entry ->
            if (index == 0) {
                prevPoint = entry.value
                return@filterIndexed true
            }
            val (prevLat, prevLon) = prevPoint ?: return@filterIndexed false
            val (currLat, currLon) = entry.value

            // Compute distance between last and current point
            val dist = GeoUtil.equirectangularDistance(prevLat, prevLon, currLat, currLon) * 1_000.0

            if (dist < distThreshold) {
                // If distance is under threshold, skip this point and move to the next one
                false
            } else {
                // If distance is above threshold, keep this point and use it as previous point for
                // the next calculations
                prevPoint = entry.value
                true
            }
        }
        return dbg
    }

    /**
     * Reads an concatenates all sound level values tied to the measurement with
     * the given unique identifier
     *
     * @param measurementUuid Measurement unique identifier
     * @return A list of map entries with timestamp as key and LAEq as value,
     *         sorted in ascending order by timestamp.
     */
    private suspend fun getSortedLaeqSequence(
        measurementUuid: String,
    ): List<Map.Entry<Long, Double>> {
        return measurementService.getLeqSequenceForMeasurement(measurementUuid)
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
