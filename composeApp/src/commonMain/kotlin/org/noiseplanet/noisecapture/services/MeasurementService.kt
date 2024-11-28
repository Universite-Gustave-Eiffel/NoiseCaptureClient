package org.noiseplanet.noisecapture.services

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.Measurement

interface MeasurementService {

    /**
     * Stores the given measurement to the database
     *
     * @param measurement Measurement to store
     */
    fun storeMeasurement(measurement: Measurement)

    /**
     * Gets locally stored measurements
     *
     * @return Measurements found in database
     */
    fun getMeasurements(): List<Measurement>
}


class DefaultMeasurementService : MeasurementService, KoinComponent {

    // - Constants

    companion object {

        private const val TAG = "MeasurementService"
    }


    // - Properties

    private val logger: Logger by inject { parametersOf(TAG) }


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
