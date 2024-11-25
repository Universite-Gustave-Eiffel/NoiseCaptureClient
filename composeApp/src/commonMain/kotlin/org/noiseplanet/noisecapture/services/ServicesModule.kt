package org.noiseplanet.noisecapture.services

import org.koin.dsl.module

val servicesModule = module {

    single<PermissionService> { DefaultPermissionService() }

    single<LiveAudioService> {
        DefaultLiveAudioService(
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
