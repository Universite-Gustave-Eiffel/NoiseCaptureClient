package org.noiseplanet.noisecapture.permission

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.noiseplanet.noisecapture.permission.delegate.PermissionDelegate

interface PermissionService : KoinComponent {

    fun checkPermission(permission: Permission): PermissionState
    fun requestPermission(permission: Permission)
}


internal class PermissionServiceImpl : PermissionService {

    // TODO: Provide a permission listener using coroutines

    override fun checkPermission(permission: Permission): PermissionState {
        val delegate: PermissionDelegate = get(named(permission.name))
        return delegate.getPermissionState()
    }

    override fun requestPermission(permission: Permission) {
        val delegate: PermissionDelegate = get(named(permission.name))
        delegate.providePermission()
    }
}