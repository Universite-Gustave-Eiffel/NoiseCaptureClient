package org.noiseplanet.noisecapture.ui.navigation

import kotlinx.serialization.Serializable


/**
 * Base interface shared by all routes.
 *
 * @param usesAudioSource If set to true, audio source will be started automatically when
 *                        navigating to this screen. If false, it will be paused instead.
 */
@Serializable
open class Route(
    val id: String,
    val usesAudioSource: Boolean = false,
)


// - Routes

@Serializable
class HomeRoute : Route(id = "home", usesAudioSource = true)

@Serializable
class MeasurementRecordingRoute : Route(id = "measurement_recording", usesAudioSource = true)

@Serializable
class HistoryRoute : Route(id = "history")

@Serializable
class SettingsRoute : Route(id = "settings")

@Serializable
class MeasurementDetailsRoute(
    val measurementId: String,
    val parentRouteId: String,
) : Route(id = "measurement/$measurementId")
