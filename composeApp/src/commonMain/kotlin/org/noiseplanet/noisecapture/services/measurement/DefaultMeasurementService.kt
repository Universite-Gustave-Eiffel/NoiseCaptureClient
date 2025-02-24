package org.noiseplanet.noisecapture.services.measurement

import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.Measurement
import org.noiseplanet.noisecapture.util.injectLogger

/**
 * Default [MeasurementService] implementation
 */
class DefaultMeasurementService : MeasurementService, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()


    // - MeasurementService

    /**
     * Stores the given measurement to the database
     */
    override fun storeMeasurement(measurement: Measurement) {
        logger.debug("Store measurement: ${measurement.id}")
        logger.debug("Acoustic indicators data points: ${measurement.acousticIndicators.size}")
        logger.debug("User location data points: ${measurement.userLocationHistory.size}")

        // TODO: Actually store measurement
    }

    /**
     * Gets locally stored measurements
     */
    override fun getMeasurements(): List<Measurement> {
        // TODO: Fetch measurements from database
        return emptyList()
    }
}
