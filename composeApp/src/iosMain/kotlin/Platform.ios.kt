import org.noiseplanet.noisecapture.permission.Permission
import platform.UIKit.UIDevice

@Suppress("MatchingDeclarationName")
class IOSPlatform : Platform {

    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override val requiredPermissions: List<Permission>
        get() = listOf(
            Permission.RECORD_AUDIO,
            Permission.LOCATION_FOREGROUND,
            Permission.LOCATION_SERVICE_ON,
            Permission.LOCATION_BACKGROUND
        )
}
