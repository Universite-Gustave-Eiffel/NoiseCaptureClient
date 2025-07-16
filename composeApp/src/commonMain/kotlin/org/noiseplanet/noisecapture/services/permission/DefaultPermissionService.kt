package org.noiseplanet.noisecapture.services.permission

import kotlinx.coroutines.flow.MutableStateFlow
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

    // - Properties

    /**
     * Current state of each permission. Will be updated when performing permission checks or
     * whenever the app goes back to foreground state.
     */
    private var permissionSates: Map<Permission, MutableStateFlow<PermissionState>> =
        Permission.entries.associateWith {
            MutableStateFlow(PermissionState.NOT_DETERMINED)
        }.toMap()


    // - PermissionService

    override fun getPermissionStateFlow(permission: Permission): StateFlow<PermissionState> {
        val permissionFlow = permissionSates[permission]
        checkNotNull(permissionFlow)

        return permissionFlow
    }

    override fun checkPermission(permission: Permission): PermissionState {
        return getPermissionDelegate(permission).permissionStateFlow.value
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
