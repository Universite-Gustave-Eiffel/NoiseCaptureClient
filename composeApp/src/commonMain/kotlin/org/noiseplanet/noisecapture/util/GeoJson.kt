package org.noiseplanet.noisecapture.util

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Json serializer to be used to encode GeoJson objects.
 * Setup to encode defaults (feature types), not encode null values (optional properties),
 * and use pretty print for easier exploitation.
 */
val GeoJson = Json {
    encodeDefaults = true
    explicitNulls = false
    prettyPrint = true
}

/**
 * Marker interface to indicate a GeoJson object. It can be
 * a Geometry, a Feature or a FeatureCollection
 */
@Serializable
sealed class GeoJsonObject

/**
 * Marker for supported geometries (Point, LineString, Polygon, ...)
 */
@Serializable
sealed class Geometry : GeoJsonObject()

/**
 * Position is an alias on a DoubleArray that represents the coordinates
 * in degrees for longitude (index = 0), latitude (index = 1) and altitude (meters).
 * The altitude is not a mandatory information. The position can be 2 length array or
 * a 3 length array (with altitude)
 */
typealias Position = DoubleArray

/**
 * Longitude value.
 */
val Position.lon: Double
    get() = this[0]

/**
 * Latitude value.
 */
val Position.lat: Double
    get() = this[1]

/**
 * Altitude value, or null.
 */
val Position.alt: Double?
    get() = if (size > 2) this[2] else null

/**
 * New Position from [Lat, Lon, (Alt)] coordinates.
 */
fun positionOf(vararg coordinates: Double): Position {
    require(coordinates.size in 2..3) { "Coordinates must be [Lat, Lon] or [Lat, Lon, Alt]" }
    return doubleArrayOf(*coordinates)
}


/**
 * Type alias for an array of Positions.
 */
typealias Line = List<Position>

/**
 * New [Line] from list of positions.
 */
fun lineOf(vararg positions: Position): Line = listOf(*positions)


/**
 * Features
 */

/**
 * A feature contains a Geometry and optional properties.
 * Since the type is not polymorphic, we must add the "type" property manually.
 */
@Serializable
@SerialName("Feature")
data class Feature(
    val type: String = "Feature",
    val geometry: Geometry,
    val properties: Map<String, JsonElement?>? = null,
) : GeoJsonObject()

/**
 * A collection of features.
 * Since the type is not polymorphic, we must add the "type" property manually.
 */
@Serializable
data class FeatureCollection(
    val type: String = "FeatureCollection",
    val features: List<Feature>,
) : GeoJsonObject()

/**
 * A point is a set of Lat,Lon,(Alt) coordinates
 */
@Serializable
@SerialName("Point")
data class Point(val coordinates: Position) : Geometry() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Point

        return coordinates.contentEquals(other.coordinates)
    }

    override fun hashCode(): Int {
        return coordinates.contentHashCode()
    }
}

@Serializable
@SerialName("MultiPoint")
data class MultiPoint(val coordinates: List<Position>) : Geometry()

@Serializable
@SerialName("LineString")
data class LineString(val coordinates: Line) : Geometry()

@Serializable
@SerialName("MultiLineString")
data class MultiLineString(val coordinates: List<Line>) : Geometry()

@Serializable
@SerialName("Polygon")
data class Polygon(val coordinates: List<Line>) : Geometry()
