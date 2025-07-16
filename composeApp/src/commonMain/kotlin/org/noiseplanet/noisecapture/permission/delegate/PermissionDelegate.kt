package org.noiseplanet.noisecapture.permission.delegate

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.noiseplanet.noisecapture.permission.PermissionState


/**
 * Describes the expected behaviour of platform specific delegates.
 * Each platform should provide a delegate implementation for each permission used in the app.
 */
internal interface PermissionDelegate {

    // - Properties

    val permissionStateFlow: StateFlow<PermissionState>


    // - Public functions

    fun checkPermissionState()
    fun providePermission()
    fun openSettingPage()
}


/**
 * A default implementation for permissions that are not implemented on the current platform.
 */
internal class NotImplementedPermissionDelegate : PermissionDelegate {

    // - Properties

    override val permissionStateFlow: StateFlow<PermissionState> =
        MutableStateFlow(PermissionState.NOT_IMPLEMENTED)


    // - Public functions

    override fun checkPermissionState() {
    }

    override fun providePermission() {
    }

    override fun openSettingPage() {
    }
}
