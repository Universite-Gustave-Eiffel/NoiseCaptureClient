import org.noiseplanet.noisecapture.model.dao.UserAgent
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
}
