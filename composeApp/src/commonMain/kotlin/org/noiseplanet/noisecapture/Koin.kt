package org.noiseplanet.noisecapture

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.noiseplanet.noisecapture.permission.PermissionService
import org.noiseplanet.noisecapture.permission.PermissionServiceImpl
import org.noiseplanet.noisecapture.permission.defaultPermissionModule
import org.noiseplanet.noisecapture.permission.platformPermissionModule

/**
 * Create root Koin application and register modules shared between platforms
 */
fun initKoin(
    additionalModules: List<Module> = emptyList(),
): KoinApplication {
    val koinApplication = koinApplication {
        modules(
            listOf(
                module {
                    includes(additionalModules)
                    includes(defaultPermissionModule, platformPermissionModule())

                    single<PermissionService> { PermissionServiceImpl() }
                }
            )
        )
        createEagerInstances()
    }
    return startKoin(koinApplication)
}
