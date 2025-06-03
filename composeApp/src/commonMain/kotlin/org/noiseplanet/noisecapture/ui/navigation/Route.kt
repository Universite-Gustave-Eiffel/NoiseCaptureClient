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
    val usesAudioSource: Boolean = false,
)


// - Routes

@Serializable
class HomeRoute : Route(usesAudioSource = true)

@Serializable
class PlatformInfoRoute : Route()

@Serializable
class RequestPermissionRoute : Route()

@Serializable
class MeasurementRecordingRoute : Route(usesAudioSource = true)

@Serializable
class HistoryRoute : Route()

@Serializable
class SettingsRoute : Route()

@Serializable
class MeasurementDetailsRoute(
    val measurementId: String,
) : Route()

@Serializable
class DebugRoute : Route()
