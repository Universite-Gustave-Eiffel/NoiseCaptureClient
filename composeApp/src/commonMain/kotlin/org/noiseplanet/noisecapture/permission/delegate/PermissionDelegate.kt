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

    /**
     * A flow of permission state values.
     */
    val permissionStateFlow: StateFlow<PermissionState>


    // - Public functions

    /**
     * Checks the current permission state, and emits it through the state flow.
     */
    fun checkPermissionState()

    /**
     * Opens permission request dialog if possible.
     */
    fun providePermission()


    /**
     * Returns true if this permission delegate can automatically redirect the user
     * to the system settings page corresponding to this permission.
     */
    fun canOpenSettings(): Boolean

    /**
     * Opens the associated settings page, if possible.
     */
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

    override fun canOpenSettings(): Boolean = false

    override fun openSettingPage() {
    }
}
