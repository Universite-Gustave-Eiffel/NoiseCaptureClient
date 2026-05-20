package org.noiseplanet.noisecapture.signal.geojson

import org.noiseplanet.noisecapture.util.geo.GeoJson
import org.noiseplanet.noisecapture.util.geo.alt
import org.noiseplanet.noisecapture.util.geo.lat
import org.noiseplanet.noisecapture.util.geo.lon
import org.noiseplanet.noisecapture.util.geo.positionOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull


/**
 * Tests GeoJson's Position object
 * https://datatracker.ietf.org/doc/html/rfc7946#section-3.1.1
 */
class PositionTest {

    @Test
    fun testXY() {
        val expected = """
            [
                100.0,
                0.0
            ]
        """.trimIndent()

        val position = positionOf(100.0, 0.0)
        assertEquals(expected, GeoJson.encodeToString(position))
    }

    @Test
    fun testXYZ() {
        val expected = """
            [
                100.0,
                0.0,
                50.0
            ]
        """.trimIndent()

        val position = positionOf(100.0, 0.0, 50.0)
        assertEquals(expected, GeoJson.encodeToString(position))
    }

    @Test
    fun testLatLon() {
        val position = positionOf(100.0, 0.0)
        assertEquals(100.0, position.lon)
        assertEquals(0.0, position.lat)
        assertNull(position.alt)
    }

    @Test
    fun testLatLonAlt() {
        val position = positionOf(100.0, 0.0, 50.0)
        assertEquals(100.0, position.lon)
        assertEquals(0.0, position.lat)
        assertEquals(50.0, position.alt)
    }

    @Test
    fun testInvalidCoordinates() {
        assertFailsWith(IllegalArgumentException::class) {
            positionOf()
        }
        assertFailsWith(IllegalArgumentException::class) {
            positionOf(1.0)
        }
        assertFailsWith(IllegalArgumentException::class) {
            positionOf(1.0, 2.0, 3.0, 4.0)
        }
    }
}
