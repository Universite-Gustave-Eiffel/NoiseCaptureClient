package org.noiseplanet.noisecapture.services.measurement

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
