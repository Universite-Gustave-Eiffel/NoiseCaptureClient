package org.noiseplanet.noisecapture.ui.navigation

import kotlinx.serialization.Serializable


/**
 * A unique route identifier, regardless of eventual route constructor parameters
 */
@Serializable
enum class RouteId {

    HOME,
    MEASUREMENT_RECORDING,
    HISTORY,
    SETTINGS,
    MEASUREMENT_DETAILS,
}


/**
 * Base interface shared by all routes.
 *
 * @param usesAudioSource If set to true, audio source will be started automatically when
 *                        navigating to this screen. If false, it will be paused instead.
 */
@Serializable
open class Route(
    val id: RouteId,
    val usesAudioSource: Boolean = false,
)


// - Routes

@Serializable
class HomeRoute : Route(id = RouteId.HOME, usesAudioSource = true)

@Serializable
class MeasurementRecordingRoute : Route(id = RouteId.MEASUREMENT_RECORDING, usesAudioSource = true)

@Serializable
class HistoryRoute : Route(id = RouteId.HISTORY)

@Serializable
class SettingsRoute : Route(id = RouteId.SETTINGS)

@Serializable
class MeasurementDetailsRoute(
    val measurementId: String,
    val parentRouteId: String,
) : Route(id = RouteId.MEASUREMENT_DETAILS)
