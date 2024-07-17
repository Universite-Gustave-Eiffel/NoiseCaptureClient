package org.noiseplanet.noisecapture.permission

internal class WasmJSPermissionService : PermissionService {

    override fun checkPermission(permission: Permission): PermissionState {
        return PermissionState.DENIED
    }

    override fun requestPermission(permission: Permission) {
        // TODO: Request permission for this platform
    }
}

actual fun getPermissionService(): PermissionService = WasmJSPermissionService()
