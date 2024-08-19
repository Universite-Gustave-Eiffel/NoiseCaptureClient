package org.noiseplanet.noisecapture.permission

import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.noiseplanet.noisecapture.permission.delegate.NotImplementedPermissionDelegate
import org.noiseplanet.noisecapture.permission.delegate.PermissionDelegate

/**
 * Should be implemented in each platform to inject custom permission delegate implementations
 */
internal expect fun platformPermissionModule(): Module


internal val defaultPermissionModule = module {

    single<PermissionService> { DefaultPermissionService() }

    for (permission in Permission.entries) {
        // Register a default delegate implementation for each permission that will be overridden
        // in each platform module depending on the supported permissions
        single<PermissionDelegate>(named(permission.name)) {
            NotImplementedPermissionDelegate()
        }
    }
}
