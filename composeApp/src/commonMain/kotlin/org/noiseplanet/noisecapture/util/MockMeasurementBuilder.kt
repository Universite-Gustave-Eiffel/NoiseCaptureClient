package org.noiseplanet.noisecapture.util

import org.noiseplanet.noisecapture.model.dao.LeqRecord
import org.noiseplanet.noisecapture.model.dao.LocationRecord
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import ovh.plrapps.mapcompose.utils.lerp
import kotlin.math.floor
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class MockMeasurementBuilder(
    private val measurementService: MeasurementService,
) {

    /**
     * Generate a dummy measurement with random sound levels and location data.
     *
     * @param leqSequenceLength Number of sound level values in measurement.
     * @param locationSequenceLength Number of generated location points.
     *
     * @return UUID of the generated measurement.
     */
    suspend fun make(
        leqSequenceLength: Int,
        locationSequenceLength: Int,
    ): String? {
        val uuid = "debug_" + Uuid.random().toString()
        val startTime = Clock.System.now().toEpochMilliseconds()
        var currentTime = startTime
        val leqSequenceInterval: Long = 125
        val locationSequenceInterval: Long = 1_000

        // Use a 1D perlin noise generator for smooth random numbers
        val perlin = PerlinNoise1D(seed = Random.nextInt())

        measurementService.openOngoingMeasurement(uuid)

        // Start at 50dB
        repeat(leqSequenceLength) {
            // Vary current LAEq value somewhat smoothly while keeping it random
            val leq = 50.0 + perlin.noise((currentTime - startTime) / 7_917.0) * 15.0

            measurementService.pushToOngoingMeasurement(
                LeqRecord(
                    timestamp = currentTime,
                    lzeq = leq.roundTo(2),
                    lceq = leq.roundTo(2),
                    laeq = leq.roundTo(2),
                    leqsPerThirdOctave = emptyMap() // TODO: LEq per third octave
                )
            )
            currentTime += leqSequenceInterval
        }

        // Start at 48.0 : 7.0
        var currentLat = 48.0
        var currentLon = 7.0

        currentTime = startTime
        repeat(locationSequenceLength) {
            // TODO: Follow an actual path?
            // Use perlin noise to move latitude and longitude smoothly (will give weird loopy paths,
            // but more random than second approach)
            // currentLat += perlin.noise((currentTime - startTime) / 16_497.31) * 0.0001 + 0.00005
            // currentLon += perlin.noise((currentTime - startTime) / 17_125.98) * 0.0001 + 0.00005

            // Use random number with a bias to move randomly in a certain direction.
            // This will give a more step-like feel that resembles more an actual walking path,
            // but is less random.
            currentLat -= 0.00001 + Random.nextDouble(
                from = -0.00002,
                until = 0.00002
            ) // Vary by roughly 1 meter
            currentLon -= 0.00001 + Random.nextDouble(
                from = -0.00002,
                until = 0.00002
            ) // Vary by roughly 1 meter

            measurementService.pushToOngoingMeasurement(
                LocationRecord(
                    timestamp = currentTime,
                    lat = currentLat,
                    lon = currentLon,
                    speed = null,
                    altitude = null,
                    direction = null,
                    orientation = null,
                    horizontalAccuracy = 100.0,
                    verticalAccuracy = null,
                    speedAccuracy = null,
                    directionAccuracy = null,
                    orientationAccuracy = null,
                )
            )
            currentTime += locationSequenceInterval
        }

        measurementService.closeOngoingMeasurement()
        return uuid
    }
}


private class PerlinNoise1D(private val seed: Int = 0) {

    private val p: IntArray = IntArray(512).apply {
        val permutation = (0..255).toMutableList().also { it.shuffle(Random(seed)) }
        permutation.addAll(permutation)
        permutation.toIntArray().copyInto(this)
    }

    private fun fade(t: Double): Double {
        return t * t * t * (t * (t * 6 - 15) + 10)
    }

    private fun grad(hash: Int, x: Double): Double {
        val h = hash and 15
        val grad = 1.0 + (h and 7)
        return (if (h and 8 != 0) -grad else grad) * x
    }

    fun noise(x: Double): Double {
        val xi = floor(x).toInt() and 255
        val xf = x - floor(x)

        val u = fade(xf)
        val a = p[xi] and 255
        val b = p[xi + 1] and 255

        return lerp(grad(p[a], xf), grad(p[b], xf - 1), u)
    }
}
