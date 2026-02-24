package org.noiseplanet.noisecapture.ui.navigation

import kotlinx.serialization.Serializable


/**
 * A unique route identifier, regardless of eventual route constructor parameters
 */
object RouteIds {

    const val HOME: RouteId = "home"
    const val RECORDING: RouteId = "recording"
    const val HISTORY: RouteId = "history"
    const val SETTINGS: RouteId = "settings"
    const val DETAILS: RouteId = "details"
    const val COMMUNITY_MAP: RouteId = "map"

    const val CALIBRATION_CONFIG: RouteId = "calibration"
    const val CALIBRATION: RouteId = "calibration/analysis"

    // Naming this route "DEBUG" breaks compilation on iOS because it gets interpreted as and
    // obj-C macro.
    const val DEBUG_ROUTE: RouteId = "debug"
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
) {

    // - Static constructor

    companion object {

        /**
         * Builds a [Route] from URL path (e.g. `details/{id}`), if possible.
         *
         * @param urlPath URL path (e.g. `details/{id}`)
         * @return Corresponding route, or null if couldn't match to any route.
         */
        fun fromUrlPath(urlPath: String): Route? {
            val pathComponents = urlPath.split("/")
            val routeId = pathComponents.firstOrNull() ?: return null

            return when (routeId) {
                RouteIds.HOME -> HomeRoute()
                RouteIds.RECORDING -> RecordingRoute()
                RouteIds.HISTORY -> HistoryRoute()
                RouteIds.SETTINGS -> SettingsRoute()
                RouteIds.COMMUNITY_MAP -> CommunityMapRoute()
                RouteIds.DEBUG_ROUTE -> DebugRoute()

                RouteIds.DETAILS -> {
                    val measurementId = pathComponents.getOrNull(1) ?: return null
                    DetailsRoute(
                        measurementId = measurementId,
                        parentRouteId = RouteIds.HOME, // TODO: Can this work?
                    )
                }

                else -> null
            }
        }
    }


    /**
     * Represents this route as a URL path (e.g. details/242?parameter=true).
     * By default, only returns the route's id.
     */
    open fun toUrlPath(): String {
        return id
    }
}


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
) : Route(id = RouteIds.DETAILS) {

    override fun toUrlPath(): String {
        return "$id/$measurementId"
    }
}

@Serializable
class CommunityMapRoute : Route(id = RouteIds.COMMUNITY_MAP)

@Serializable
class CalibrationConfigRoute : Route(id = RouteIds.CALIBRATION_CONFIG)

@Serializable
class CalibrationRoute : Route(id = RouteIds.CALIBRATION)

@Serializable
class DebugRoute : Route(id = RouteIds.DEBUG_ROUTE)
