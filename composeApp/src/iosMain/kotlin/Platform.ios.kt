import org.noiseplanet.noisecapture.model.dao.UserAgent
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.navigation.Route
import platform.UIKit.UIDevice

@Suppress("MatchingDeclarationName")
class IOSPlatform : Platform {

    override val userAgent: UserAgent
        get() = UserAgent(
            deviceManufacturer = "Apple",
            deviceModelCode = UIDevice.currentDevice.model,
            deviceModelName = UIDevice.currentDevice.name,
            osName = UIDevice.currentDevice.systemName,
            osVersion = UIDevice.currentDevice.systemVersion,
        )

    override val requiredPermissions: Map<Route, List<Permission>>
        get() = listOf(
            Permission.RECORD_AUDIO,
            Permission.LOCATION_FOREGROUND,
            Permission.LOCATION_SERVICE_ON,
            Permission.LOCATION_BACKGROUND
        )
}
