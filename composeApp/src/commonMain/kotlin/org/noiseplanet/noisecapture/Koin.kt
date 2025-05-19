package org.noiseplanet.noisecapture

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.noiseplanet.noisecapture.permission.defaultPermissionModule
import org.noiseplanet.noisecapture.permission.platformPermissionModule
import org.noiseplanet.noisecapture.services.servicesModule
import org.noiseplanet.noisecapture.ui.features.screensModule
import org.noiseplanet.noisecapture.ui.navigation.coordinatorModule

/**
 * Create root Koin application and register modules shared between platforms
 */
fun initKoin(
    additionalModules: List<Module> = emptyList(),
): KoinApplication {

    stopKoin()

    return startKoin {
        modules(
            servicesModule,
            coordinatorModule,

            defaultPermissionModule,
            platformPermissionModule(),

            screensModule,

            module {
                includes(additionalModules)
            },
        )
        createEagerInstances()
    }
}
