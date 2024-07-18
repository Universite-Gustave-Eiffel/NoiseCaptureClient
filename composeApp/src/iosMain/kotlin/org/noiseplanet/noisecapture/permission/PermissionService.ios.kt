package org.noiseplanet.noisecapture.permission

/**
 * iOS implementation of PermissionService
 */
internal class IOSPermissionService : PermissionService {

    override fun checkPermission(permission: Permission): PermissionState {
        return PermissionState.GRANTED
    }

    override fun requestPermission(permission: Permission) {
        // TODO: Request permission for this platform
    }
}

// TODO: Can we use expect/actual classes directly?
actual fun getPermissionService(): PermissionService = IOSPermissionService()
