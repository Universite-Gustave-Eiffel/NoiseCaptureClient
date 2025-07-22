import org.noiseplanet.noisecapture.interop.navigator
import org.noiseplanet.noisecapture.model.dao.UserAgent
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.navigation.RouteId
import org.noiseplanet.noisecapture.ui.navigation.RouteIds

class WasmJSPlatform : Platform {

    override val userAgent: UserAgent
        // Getting device information on web can be tricky due to the variety of
        // browsers and devices. For now, this is the approach we take:
        get() = UserAgent(
            // On some browsers, will return the name of the *browser* manufacturer (e.g. Google Inc.)
            deviceManufacturer = navigator?.vendor,
            // Should return the platform running the browser (kind of device name, e.g. MacIntel)
            deviceModelName = navigator?.platform,
            // Returns either a fixed version number or a more detailed browser version string.
            // In most browsers this is included as part of user agent.
            deviceModelCode = navigator?.appCodeName,
            // Most device information will be contained here but the format can change from browser
            // to browser so better include the raw string than trying to parse it.
            osVersion = navigator?.userAgent,
            // Using navigator.oscpu only works on Firefox an crashes on other browsers so we
            // can't rely on that. To filter based on OS, one should rely on [osVersion] instead.
            osName = "WasmJS",
        )

    override val optionalPermissions: Map<RouteId, List<Permission>>
        // We can't control laptop settings on the web so we don't
        // check if location services are on. It will be part of the
        // location background permission check.
        get() = super.optionalPermissions + mapOf(
            RouteIds.MEASUREMENT_RECORDING to listOf(
                Permission.LOCATION_BACKGROUND,
            )
        )
}
