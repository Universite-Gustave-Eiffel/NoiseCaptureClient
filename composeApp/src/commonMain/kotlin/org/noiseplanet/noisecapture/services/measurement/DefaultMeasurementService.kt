package org.noiseplanet.noisecapture.services.measurement

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.model.dao.MutableMeasurement
import org.noiseplanet.noisecapture.services.storage.StorageService
import org.noiseplanet.noisecapture.util.injectLogger

/**
 * Default [MeasurementService] implementation
 */
class DefaultMeasurementService : MeasurementService, KoinComponent {

    // - Properties


    private val logger: Logger by injectLogger()
    private val storageService: StorageService<Measurement> by inject()


    // - MeasurementService

    override suspend fun storeMeasurement(mutableMeasurement: MutableMeasurement) {


        logger.debug("Storing measurement: ${measurement.uuid}")
        logger.debug("Leqs sequence length: ${mutableMeasurement.leqsSequence.size}")
        logger.debug("Location sequence length: ${mutableMeasurement.locationSequence.size}")

        storageService.set(measurement.uuid, measurement)
    }

    /**
     * Gets locally stored measurements
     */
    override suspend fun getMeasurements(): List<Measurement> {
        // TODO: Fetch measurements from database
        return storageService.getAll()
    }
}
