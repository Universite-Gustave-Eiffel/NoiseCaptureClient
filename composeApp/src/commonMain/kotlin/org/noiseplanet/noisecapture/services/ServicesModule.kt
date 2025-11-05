package org.noiseplanet.noisecapture.services

import org.koin.dsl.module
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.services.audio.DefaultLiveAudioService
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.location.DefaultUserLocationService
import org.noiseplanet.noisecapture.services.location.UserLocationService
import org.noiseplanet.noisecapture.services.measurement.DefaultMeasurementService
import org.noiseplanet.noisecapture.services.measurement.DefaultRecordingService
import org.noiseplanet.noisecapture.services.measurement.MeasurementService
import org.noiseplanet.noisecapture.services.measurement.RecordingService
import org.noiseplanet.noisecapture.services.permission.DefaultPermissionService
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.services.settings.DefaultUserSettingsService
import org.noiseplanet.noisecapture.services.settings.UserSettingsService
import org.noiseplanet.noisecapture.services.storage.MeasurementStorageService
import org.noiseplanet.noisecapture.services.storage.kstore.KStoreStorageService
import org.noiseplanet.noisecapture.services.storage.singleStorageService

val servicesModule = module {

    single<PermissionService> {
        DefaultPermissionService()
    }

    single<LiveAudioService> {
        DefaultLiveAudioService()
    }

    single<UserSettingsService> {
        DefaultUserSettingsService(
            settingsProvider = get(),
        )
    }

    single<UserLocationService> {
        DefaultUserLocationService()
    }

    single<RecordingService> {
        DefaultRecordingService()
    }

    single<MeasurementService> {
        DefaultMeasurementService()
    }


    // - Storage

    singleStorageService {
        MeasurementStorageService()
    }

    singleStorageService {
        KStoreStorageService(
            prefix = "measurement/leqs",
            type = LeqSequenceFragment::class,
        )
    }

    singleStorageService {
        KStoreStorageService(
            prefix = "measurement/locations",
            type = LocationSequenceFragment::class,
        )
    }
}
