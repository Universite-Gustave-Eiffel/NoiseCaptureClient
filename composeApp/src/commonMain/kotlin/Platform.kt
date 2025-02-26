import org.noiseplanet.noisecapture.model.UserAgent
import org.noiseplanet.noisecapture.permission.Permission

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
     * Permissions required to run the app on this platform.
     * Those can differ based on which features are made available for the app or the amount
     * of control given to the user on those permissions.
     */
    val requiredPermissions: List<Permission>
        get() = listOf(
            Permission.RECORD_AUDIO,
            Permission.LOCATION_BACKGROUND,
            Permission.LOCATION_SERVICE_ON
        )
}
