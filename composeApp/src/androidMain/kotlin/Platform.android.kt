import android.os.Build
import org.noiseplanet.noisecapture.BuildKonfig
import org.noiseplanet.noisecapture.model.dao.UserAgent
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.navigation.RouteId
import org.noiseplanet.noisecapture.ui.navigation.RouteIds


class AndroidPlatform : Platform {

    override val userAgent: UserAgent
        get() = UserAgent(
            versionName = BuildKonfig.versionName,
            versionCode = BuildKonfig.versionCode,
            deviceManufacturer = Build.MANUFACTURER,
            deviceModelName = Build.DEVICE,
            deviceModelCode = Build.PRODUCT,
            osName = "Android",
            osVersion = "${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})",
        )

    override val requiredPermissions: Map<RouteId, List<Permission>>
        // To get our foreground service to run while the app is in background we need
        // the POST_NOTIFICATIONS permission to display a persistent notification.
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return super.requiredPermissions + mapOf(
                    RouteIds.MEASUREMENT_RECORDING to listOf(
                        Permission.RECORD_AUDIO,
                        Permission.POST_NOTIFICATIONS,
                    )
                )
            } else {
                return super.requiredPermissions + mapOf(
                    RouteIds.MEASUREMENT_RECORDING to listOf(
                        Permission.RECORD_AUDIO
                    )
                )
            }
        }

    override val optionalPermissions: Map<RouteId, List<Permission>>
        // On Android we don't need background location, but instead we only use
        // foreground location with a ForegroundService to keep the location
        // updates coming.
        get() = super.optionalPermissions + mapOf(
            RouteIds.MEASUREMENT_RECORDING to listOf(
                Permission.LOCATION_SERVICE_ON,
                Permission.LOCATION_FOREGROUND,
            )
        )
}
