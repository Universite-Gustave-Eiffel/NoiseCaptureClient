class WasmJSPlatform : Platform {

    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmJSPlatform()
