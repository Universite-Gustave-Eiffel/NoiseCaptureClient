import org.noiseplanet.noisecapture.model.dao.UserAgent
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.navigation.HomeRoute
import org.noiseplanet.noisecapture.ui.navigation.MeasurementRecordingRoute

/**
 * Describes a platform running the app.
 */
interface Platform {

    /**
     * Information about the device this app is running on,
     * as well as the app version that is currently running.
     */
    val userAgent: UserAgent

    /**
     * Required permissions to access features of certain app screens.
     * Can be overridden per platform to ask for platform specific permissions.
     *
     * TODO: Figure out a more robust way to check for route.
     */
    val requiredPermissions: Map<String, List<Permission>>
        get() = mapOf(
            MeasurementRecordingRoute().id to listOf(Permission.RECORD_AUDIO)
        )

    /**
     * Optional permissions to access all features of certain screens.
     * Will be prompted to the end user when opening the screen if not granted but can be dismissed.
     * Can be overridden per platform to ask for platform specific permissions.
     *
     * TODO: Figure out a more robust way to check for route.
     */
    val optionalPermissions: Map<String, List<Permission>>
        get() = mapOf(
            HomeRoute().id to listOf(Permission.RECORD_AUDIO),
            MeasurementRecordingRoute().id to listOf(
                Permission.LOCATION_SERVICE_ON,
                Permission.LOCATION_FOREGROUND,
            )
        )
}
