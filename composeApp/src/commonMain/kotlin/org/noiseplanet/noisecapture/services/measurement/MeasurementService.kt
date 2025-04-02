package org.noiseplanet.noisecapture.services.measurement

import org.noiseplanet.noisecapture.model.measurement.Measurement
import org.noiseplanet.noisecapture.model.measurement.MutableMeasurement

interface MeasurementService {

    /**
     * Creates a new immutable measurement from mutable entry and stores it into the database
     *
     * @param mutableMeasurement Measurement to store
     */
    fun storeMeasurement(mutableMeasurement: MutableMeasurement)

    /**
     * Gets locally stored measurements
     *
     * @return Measurements found in database
     */
    fun getMeasurements(): List<Measurement>
}
