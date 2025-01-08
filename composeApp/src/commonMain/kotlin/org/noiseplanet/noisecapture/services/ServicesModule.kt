package org.noiseplanet.noisecapture.services

import org.koin.dsl.module
import org.noiseplanet.noisecapture.services.audio.DefaultLiveAudioService
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.location.DefaultUserLocationService
import org.noiseplanet.noisecapture.services.location.UserLocationService
import org.noiseplanet.noisecapture.services.measurement.DefaultMeasurementRecordingService
import org.noiseplanet.noisecapture.services.measurement.DefaultMeasurementService
import org.noiseplanet.noisecapture.services.measurement.MeasurementRecordingService
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.services.permission.DefaultPermissionService
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.services.settings.DefaultUserSettingsService
import org.noiseplanet.noisecapture.services.settings.UserSettingsService

val servicesModule = module {

    single<PermissionService> {
        DefaultPermissionService()
    }

    single<LiveAudioService> {
        DefaultLiveAudioService(
            audioSource = get(),
        )
    }

    single<UserSettingsService> {
        DefaultUserSettingsService(
            settingsProvider = get(),
        )
    }

    single<UserLocationService> {
        DefaultUserLocationService()
    }

    single<MeasurementService> {
        DefaultMeasurementService()
    }

    single<MeasurementRecordingService> {
        DefaultMeasurementRecordingService(
            measurementService = get(),
            liveAudioService = get(),
            userLocationService = get(),
            audioRecordingService = get(),
        )
    }
}
