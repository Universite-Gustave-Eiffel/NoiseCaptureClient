package org.noiseplanet.noisecapture.permission.delegate

import org.noiseplanet.noisecapture.permission.PermissionState


/**
 * Describes the expected behaviour of platform specific delegates.
 * Each platform should provide a delegate implementation for each permission used in the app.
 */
internal interface PermissionDelegate {

    suspend fun getPermissionState(): PermissionState
    suspend fun providePermission()
    fun openSettingPage()
}


/**
 * A default implementation for permissions that are not implemented on the current platform.
 */
internal class NotImplementedPermissionDelegate : PermissionDelegate {

    override suspend fun getPermissionState(): PermissionState {
        return PermissionState.NOT_IMPLEMENTED
    }

    override suspend fun providePermission() {
    }

    override fun openSettingPage() {
    }
}
