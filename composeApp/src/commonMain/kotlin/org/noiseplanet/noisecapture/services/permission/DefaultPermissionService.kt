package org.noiseplanet.noisecapture.services.permission

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.delegate.PermissionDelegate

/**
 * Default [PermissionService] implementation
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
