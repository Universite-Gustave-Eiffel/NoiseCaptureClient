package org.noiseplanet.noisecapture.permission

import org.koin.core.component.KoinComponent

interface PermissionService : KoinComponent {

    fun checkPermission(permission: Permission): PermissionState
    fun requestPermission(permission: Permission)
}

expect fun getPermissionService(): PermissionService
