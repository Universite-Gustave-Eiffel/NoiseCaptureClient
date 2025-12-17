import androidx.compose.ui.graphics.ImageBitmapConfig
import org.noiseplanet.noisecapture.model.dao.UserAgent
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.navigation.RouteId
import org.noiseplanet.noisecapture.ui.navigation.RouteIds

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
     */
    val requiredPermissions: Map<RouteId, List<Permission>>
        get() = mapOf(
            RouteIds.RECORDING to listOf(Permission.RECORD_AUDIO)
        )

    /**
     * Optional permissions to access all features of certain screens.
     * Will be prompted to the end user when opening the screen if not granted but can be dismissed.
     * Can be overridden per platform to ask for platform specific permissions.
     */
    val optionalPermissions: Map<RouteId, List<Permission>>
        get() = mapOf(
            RouteIds.HOME to listOf(Permission.RECORD_AUDIO),
            RouteIds.RECORDING to listOf(
                Permission.LOCATION_SERVICE_ON,
                Permission.LOCATION_BACKGROUND,
            )
        )

    /**
     * Tells which bitmap configuration is the most optimized (and supported) for the current
     * device. Defaults to [ImageBitmapConfig.Rgb565] which is the lightest cross-platform
     * configuration available, but is for instance not supported on iOS simulator.
     */
    val bitmapConfig: ImageBitmapConfig
        get() = ImageBitmapConfig.Rgb565
}
