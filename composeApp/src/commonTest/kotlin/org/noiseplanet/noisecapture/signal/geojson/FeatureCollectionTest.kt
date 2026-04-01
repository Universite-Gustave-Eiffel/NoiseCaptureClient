package org.noiseplanet.noisecapture.signal.geojson

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.noiseplanet.noisecapture.util.Feature
import org.noiseplanet.noisecapture.util.FeatureCollection
import org.noiseplanet.noisecapture.util.LineString
import org.noiseplanet.noisecapture.util.Point
import org.noiseplanet.noisecapture.util.Polygon
import org.noiseplanet.noisecapture.util.lineOf
import org.noiseplanet.noisecapture.util.positionOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests serializing of GeoJson FeatureCollection objects.
 * https://datatracker.ietf.org/doc/html/rfc7946#section-1.5
 */
class FeatureCollectionTest {

    val json = Json {
        explicitNulls = false
        prettyPrint = true
        encodeDefaults = true
    }

    @Test
    @Suppress("LongMethod")
    fun testFeatureCollection() {
        val expected = """
            {
                "type": "FeatureCollection",
                "features": [
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "Point",
                            "coordinates": [
                                102.0,
                                0.5
                            ]
                        },
                        "properties": {
                            "prop0": "value0"
                        }
                    },
                    {
                        "type": "Feature",
                        "geometry": {
                            "type": "LineString",
                            "coordinates": [
                                [
                                    102.0,
                                    0.0
                                ],
                                [
                                    103.0,
                                    1.0
                                ],
                                [
                                    104.0,
                                    0.0
                                ],
                                [
                                    105.0,
                                    1.0
                                ]
                            ]
                        },
                        "properties": {
                            "prop0": "value0",
                            "prop1": 0.0
                        }
                    },
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
                        },
                        "properties": {
                            "prop0": "value0",
                            "prop1": {
                                "this": "that"
                            }
                        }
                    }
                ]
            }
        """.trimIndent()
        val featureCollection = FeatureCollection(
            features = listOf(
                Feature(
                    geometry = Point(positionOf(102.0, 0.5)),
                    properties = mapOf("prop0" to JsonPrimitive("value0")),
                ),
                Feature(
                    geometry = LineString(
                        lineOf(
                            positionOf(102.0, 0.0),
                            positionOf(103.0, 1.0),
                            positionOf(104.0, 0.0),
                            positionOf(105.0, 1.0),
                        )
                    ),
                    properties = mapOf(
                        "prop0" to JsonPrimitive("value0"),
                        "prop1" to JsonPrimitive(0.0),
                    )
                ),
                Feature(
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
                    ),
                    properties = mapOf(
                        "prop0" to JsonPrimitive("value0"),
                        "prop1" to JsonObject(mapOf("this" to JsonPrimitive("that")))
                    )
                )
            )
        )
        assertEquals(expected, json.encodeToString(featureCollection))
    }
}
