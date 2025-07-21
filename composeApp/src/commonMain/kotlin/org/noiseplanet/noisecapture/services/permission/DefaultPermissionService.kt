package org.noiseplanet.noisecapture.services.permission

import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.permission.delegate.PermissionDelegate

/**
 * Default [PermissionService] implementation
 */
internal class DefaultPermissionService : PermissionService {

    // - PermissionService

    override fun getPermissionStateFlow(permission: Permission): StateFlow<PermissionState> {
        return getPermissionDelegate(permission).permissionStateFlow
    }

    override fun getPermissionState(permission: Permission): PermissionState {
        return getPermissionDelegate(permission).permissionStateFlow.value
    }

    override fun refreshPermissionState(permission: Permission) {
        getPermissionDelegate(permission).checkPermissionState()
    }

    override fun requestPermission(permission: Permission) {
        getPermissionDelegate(permission).providePermission()
    }

    override fun openSettingsForPermission(permission: Permission) {
        getPermissionDelegate(permission).openSettingPage()
    }


    // - Private functions

    private fun getPermissionDelegate(permission: Permission): PermissionDelegate {
        return get(named(permission.name))
    }
}
