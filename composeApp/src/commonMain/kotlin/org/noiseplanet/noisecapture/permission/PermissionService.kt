package org.noiseplanet.noisecapture.permission

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.noiseplanet.noisecapture.permission.delegate.PermissionDelegate

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
    suspend fun checkPermission(permission: Permission): PermissionState

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
    suspend fun requestPermission(permission: Permission)
}


/**
 * Default Permission service implementation
 */
internal class DefaultPermissionService : PermissionService {

    private companion object {

        // Time spent between each permission check, in milliseconds
        const val PERMISSION_CHECK_FLOW_FREQUENCY = 1_000L
    }

    override fun getPermissionStateFlow(permission: Permission): Flow<PermissionState> {
        return flow {
            // Get delegate for this permission
            val delegate: PermissionDelegate = getPermissionDelegate(permission)
            // TODO: It would be nicer to provide a platform dependant listener system
            //       rather than using an infinite loop, but this will do the trick for now
            while (true) {
                val permissionState = delegate.getPermissionState()
                emit(permissionState)
                delay(PERMISSION_CHECK_FLOW_FREQUENCY)
            }
        }
    }

    override suspend fun checkPermission(permission: Permission): PermissionState {
        return getPermissionDelegate(permission).getPermissionState()
    }

    override suspend fun requestPermission(permission: Permission) {
        getPermissionDelegate(permission).providePermission()
    }

    override fun openSettingsForPermission(permission: Permission) {
        getPermissionDelegate(permission).openSettingPage()
    }

    private fun getPermissionDelegate(permission: Permission): PermissionDelegate {
        return get(named(permission.name))
    }
}
