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
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                super.requiredPermissions + mapOf(
                    RouteIds.RECORDING to listOf(
                        Permission.RECORD_AUDIO,
                        Permission.POST_NOTIFICATIONS,
                    )
                )
            } else {
                super.requiredPermissions + mapOf(
                    RouteIds.RECORDING to listOf(
                        Permission.RECORD_AUDIO
                    )
                )
            }
        }
}
