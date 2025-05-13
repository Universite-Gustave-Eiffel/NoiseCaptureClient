package org.noiseplanet.noisecapture.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Base interface shared by all routes.
 *
 * @param usesAudioSource If set to true, audio source will be started automatically when
 *                        navigating to this screen. If false, it will be paused instead.
 */
interface Route {

    val usesAudioSource: Boolean
        get() = false
}


@Serializable
data object HomeRoute : Route {

    override val usesAudioSource: Boolean
        get() = true
}

@Serializable
data object PlatformInfoRoute : Route

@Serializable
data object RequestPermissionRoute : Route

@Serializable
data object MeasurementRecordingRoute : Route {

    override val usesAudioSource: Boolean
        get() = true
}

@Serializable
data object HistoryRoute : Route

@Serializable
data object SettingsRoute : Route
