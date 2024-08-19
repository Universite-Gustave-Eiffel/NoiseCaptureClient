import org.noiseplanet.noisecapture.permission.Permission

class WasmJSPlatform : Platform {

    override val name: String = "Web with Kotlin/Wasm"

    override val requiredPermissions: List<Permission>
        // We can't control laptop settings on the web so we don't
        // check if location services are on. It will be part of the
        // location background permission check.
        get() = listOf(
            Permission.RECORD_AUDIO,
//            Permission.LOCATION_BACKGROUND
        )
}

actual fun getPlatform(): Platform = WasmJSPlatform()
