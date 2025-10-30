package org.noiseplanet.noisecapture.ui.navigation

import kotlinx.serialization.Serializable


/**
 * A unique route identifier, regardless of eventual route constructor parameters
 *
 * TODO: With current compose navigation library, using an enum for this property crashes on iOS.
 *       Check with later updates if we can use an enum instead.
 */
object RouteIds {

    const val HOME = "home"
    const val RECORDING = "recording"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val DETAILS = "details"
    const val COMMUNITY_MAP = "community_map"

    // Naming this route "DEBUG" breaks compilation on iOS because it gets interpreted as and
    // obj-C macro.
    const val DEBUG_ROUTE = "debug"
}
typealias RouteId = String


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
class HomeRoute : Route(id = RouteIds.HOME, usesAudioSource = true)

@Serializable
class RecordingRoute : Route(id = RouteIds.RECORDING, usesAudioSource = true)

@Serializable
class HistoryRoute : Route(id = RouteIds.HISTORY)

@Serializable
class SettingsRoute : Route(id = RouteIds.SETTINGS)

@Serializable
class DetailsRoute(
    val measurementId: String,
    val parentRouteId: String,
) : Route(id = RouteIds.DETAILS)

@Serializable
class CommunityMapRoute : Route(id = RouteIds.COMMUNITY_MAP)

@Serializable
class DebugRoute : Route(id = RouteIds.DEBUG_ROUTE)
