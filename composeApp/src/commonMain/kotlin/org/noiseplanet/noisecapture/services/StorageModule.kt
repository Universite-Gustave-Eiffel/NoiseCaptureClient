package org.noiseplanet.noisecapture.services

import org.koin.dsl.module
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.model.dao.UserStatistics
import org.noiseplanet.noisecapture.services.storage.MeasurementStorageService
import org.noiseplanet.noisecapture.services.storage.kstore.KStoreStorageService
import org.noiseplanet.noisecapture.services.storage.singleStorageService

val storageModule = module {

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

    singleStorageService {
        KStoreStorageService(
            prefix = "statistics",
            type = UserStatistics::class,
        )
    }
}
