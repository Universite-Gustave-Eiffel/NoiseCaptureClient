package org.noiseplanet.noisecapture

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.noiseplanet.noisecapture.measurements.DefaultMeasurementService
import org.noiseplanet.noisecapture.measurements.MeasurementsService
import org.noiseplanet.noisecapture.permission.defaultPermissionModule
import org.noiseplanet.noisecapture.permission.platformPermissionModule
import org.noiseplanet.noisecapture.ui.features.home.homeModule
import org.noiseplanet.noisecapture.ui.features.measurement.measurementModule
import org.noiseplanet.noisecapture.ui.features.permission.requestPermissionModule

/**
 * Create root Koin application and register modules shared between platforms
 */
fun initKoin(
    additionalModules: List<Module> = emptyList(),
): KoinApplication {
    return startKoin {
        modules(
            module {
                includes(additionalModules)
            },

            module {
                single<MeasurementsService> {
                    DefaultMeasurementService(audioSource = get(), logger = get())
                }
            },

            defaultPermissionModule,
            platformPermissionModule(),

            homeModule,
            requestPermissionModule,
            measurementModule,
        )
        createEagerInstances()
    }
}
