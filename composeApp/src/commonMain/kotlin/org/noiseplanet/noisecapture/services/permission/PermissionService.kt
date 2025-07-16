package org.noiseplanet.noisecapture.services.permission

import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState

/**
 * Manage the app permission states. Check current states and request permissions if needed.
 */
interface PermissionService : KoinComponent {

    /**
     * Returns a flow instance to subscribe to the given permission state updates
     *
     * @param permission Target permission
     * @return Flow of permission states
     */
    fun getPermissionStateFlow(permission: Permission): Flow<PermissionState>

    /**
     * Checks the current state of the given permission, once
     *
     * @param permission Target permission
     * @return Current permission state
     */
    fun checkPermission(permission: Permission): PermissionState

    /**
     * Opens the settings page corresponding to the given permission.
     * Should be used when permission has been previously denied since we can't trigger
     * the permission popup again.
     *
     * @param permission Target permission
     */
    fun openSettingsForPermission(permission: Permission)

    /**
     * Triggers requesting the given permission to the user
     *
     * @param permission Target permission
     */
    fun requestPermission(permission: Permission)
}
