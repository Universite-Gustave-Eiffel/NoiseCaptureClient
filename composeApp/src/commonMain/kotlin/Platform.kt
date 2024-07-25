import org.noiseplanet.noisecapture.permission.Permission

/**
 * Describes a platform running the app.
 */
interface Platform {

    /**
     * Platform name (e.g. iOS, Android, Web, ...)
     */
    val name: String

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

/**
 * Gets the [Platform] implementation for the current target.
 */
expect fun getPlatform(): Platform
