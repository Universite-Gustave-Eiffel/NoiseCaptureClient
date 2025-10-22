package org.noiseplanet.noisecapture.util

import io.github.koalaplot.core.util.Degrees
import io.github.koalaplot.core.util.toRadians
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.math.tan


/**
 * A set of utility functions for geographic calculations.
 */
object GeoUtil {

    /**
     * Converts input latitude and longitude to normalized Web Mercator coordinates.
     * Basically does a projection from a 3D coordinates system to a 2D projection.
     *
     * In the projected referential, [0, 0] is the top left corner of the map, and [1, 1] is the
     * bottom right corner. This is the coordinates system used by MapCompose.
     *
     * @param latitude Input latitude in degrees
     * @param longitude Input longitude in degrees
     *
     * @return X,Y normalized 2D coordinates.
     */
    fun lonLatToNormalizedWebMercator(
        latitude: Double,
        longitude: Double,
    ): Pair<Double, Double> {
        // Could be precomputed if optimization is needed.
        val earthRadius = 6_378_137.0 // in meters
        val piR = earthRadius * PI

        val latRad = latitude * PI / 180.0
        val lngRad = longitude * PI / 180.0

        val x = earthRadius * lngRad
        val y = earthRadius * ln(tan((PI / 4.0) + (latRad / 2.0)))

        val normalizedX = (x + piR) / (2.0 * piR)
        val normalizedY = (piR - y) / (2.0 * piR)

        return Pair(normalizedX, normalizedY)
    }

    /**
     * Calculates the distance between two geographic points using
     * [Equirectangular projection](https://en.wikipedia.org/wiki/Equirectangular_projection).
     *
     * The method is efficient in terms of computation, while still providing a good enough estimate
     * for distance between points close to one another.
     *
     * @param latA Point A latitude (in degrees)
     * @param lonA Point A longitude (in degrees)
     * @param latB Point B latitude (in degrees)
     * @param lonB Point B longitude (in degrees)
     *
     * @return Estimated distance in Km.
     */
    fun equirectangularDistance(
        latA: Double,
        lonA: Double,
        latB: Double,
        lonB: Double,
    ): Double {
        val earthRadius = 6371.0 // Earth radius in kilometers
        val averageLatitude = Degrees((latA + latB) / 2).toRadians()

        val x = Degrees(lonB - lonA).toRadians().value * cos(averageLatitude.value)
        val y = Degrees(latB - latA).toRadians().value

        return earthRadius * sqrt(x * x + y * y)
    }
}
