package org.noiseplanet.noisecapture

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
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
) {
    if (KoinPlatform.getKoinOrNull() != null) {
        // If application is already running, dont restart it.
        return
    }

    startKoin {
        allowOverride(true)

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
