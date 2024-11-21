package org.noiseplanet.noisecapture.services

import org.koin.dsl.module

val servicesModule = module {

    single<PermissionService> { DefaultPermissionService() }

    single<MeasurementsService> {
        DefaultMeasurementService(
            audioSource = get(),
            logger = get(),
        )
    }

    single<UserSettingsService> {
        DefaultUserSettingsService(
            settingsProvider = get(),
        )
    }
}
