package org.noiseplanet.noisecapture.signal.geojson

import kotlinx.serialization.json.JsonPrimitive
import org.noiseplanet.noisecapture.util.Feature
import org.noiseplanet.noisecapture.util.GeoJson
import org.noiseplanet.noisecapture.util.LineString
import org.noiseplanet.noisecapture.util.MultiLineString
import org.noiseplanet.noisecapture.util.MultiPoint
import org.noiseplanet.noisecapture.util.Point
import org.noiseplanet.noisecapture.util.Polygon
import org.noiseplanet.noisecapture.util.lineOf
import org.noiseplanet.noisecapture.util.positionOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Test serialization of GeoJson Features.
 * https://datatracker.ietf.org/doc/html/rfc7946#appendix-A
 */
class FeaturesTest {

    @Test
    fun testPoint() {
        val expected = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [
                        100.0,
                        0.0
                    ]
                }
            }
        """.trimIndent()
        val feature = Feature(geometry = Point(positionOf(100.0, 0.0)))
        assertEquals(expected, GeoJson.encodeToString(feature))
    }

    @Test
    fun testLineString() {
        val expected = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "LineString",
                    "coordinates": [
                        [
                            100.0,
                            0.0
                        ],
                        [
                            0.0,
                            100.0
                        ]
                    ]
                }
            }
        """.trimIndent()
        val feature = Feature(
            geometry = LineString(lineOf(positionOf(100.0, 0.0), positionOf(0.0, 100.0)))
        )
        assertEquals(expected, GeoJson.encodeToString(feature))
    }

    @Test
    fun testPolygon() {
        val expected = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                        [
                            [
                                100.0,
                                0.0
                            ],
                            [
                                101.0,
                                0.0
                            ],
                            [
                                101.0,
                                1.0
                            ],
                            [
                                100.0,
                                1.0
                            ],
                            [
                                100.0,
                                0.0
                            ]
                        ]
                    ]
                }
            }
        """.trimIndent()
        val feature = Feature(
            geometry = Polygon(
                listOf(
                    lineOf(
                        positionOf(100.0, 0.0),
                        positionOf(101.0, 0.0),
                        positionOf(101.0, 1.0),
                        positionOf(100.0, 1.0),
                        positionOf(100.0, 0.0),
                    )
                )
            )
        )
        assertEquals(expected, GeoJson.encodeToString(feature))
    }

    @Test
    fun testMultipoint() {
        val expected = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "MultiPoint",
                    "coordinates": [
                        [
                            100.0,
                            0.0
                        ],
                        [
                            101.0,
                            0.0
                        ]
                    ]
                }
            }
        """.trimIndent()
        val feature = Feature(
            geometry = MultiPoint(
                listOf(
                    positionOf(100.0, 0.0),
                    positionOf(101.0, 0.0),
                )
            )
        )
        assertEquals(expected, GeoJson.encodeToString(feature))
    }

    @Test
    fun testMultiLineString() {
        val expected = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "MultiLineString",
                    "coordinates": [
                        [
                            [
                                100.0,
                                0.0
                            ],
                            [
                                101.0,
                                0.0
                            ]
                        ],
                        [
                            [
                                101.0,
                                1.0
                            ],
                            [
                                100.0,
                                1.0
                            ]
                        ]
                    ]
                }
            }
        """.trimIndent()
        val feature = Feature(
            geometry = MultiLineString(
                listOf(
                    lineOf(
                        positionOf(100.0, 0.0),
                        positionOf(101.0, 0.0),
                    ),
                    lineOf(
                        positionOf(101.0, 1.0),
                        positionOf(100.0, 1.0),
                    )
                )
            )
        )
        assertEquals(expected, GeoJson.encodeToString(feature))
    }

    @Test
    fun testProperties() {
        val expected = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [
                        100.0,
                        0.0
                    ]
                },
                "properties": {
                    "doubleTest": 0.0,
                    "intTest": 1,
                    "stringTest": "string",
                    "boolTest": true
                }
            }
        """.trimIndent()
        val feature = Feature(
            geometry = Point(positionOf(100.0, 0.0)),
            properties = mapOf(
                "doubleTest" to JsonPrimitive(0.0),
                "intTest" to JsonPrimitive(1),
                "stringTest" to JsonPrimitive("string"),
                "boolTest" to JsonPrimitive(true)
            )
        )
        assertEquals(expected, GeoJson.encodeToString(feature))
    }
}
