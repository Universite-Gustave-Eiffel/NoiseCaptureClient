package org.noiseplanet.noisecapture.services.measurement

import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.model.dao.Measurement


/**
 * Interact with measurements using various storage services for the different files
 * that constitute a measurement (measurement object, leq sequence, location sequence, etc)
 */
interface MeasurementService {

    // - Properties

    /**
     * Unique identifier of the ongoing measurement, if any.
     */
    val ongoingMeasurementUuid: String?


    // - Public functions

    /**
     * Gets all stored measurements.
     *
     * > Note: This only returns the base measurement objects but not the underlying leq
     *         and location sequences.
     *
     * @return List of all stored measurements on this device
     */
    suspend fun getAllMeasurements(): List<Measurement>

    /**
     * Get a single measurement from its unique identifier.
     *
     * > Note: This only returns the base measurement objects but not the underlying leq
     *         and location sequences.
     *
     * @param uuid Measurement unique identifier
     *
     * @return Measurement object if found in local storage, null otherwise
     */
    suspend fun getMeasurement(uuid: String): Measurement?

    /**
     * Gets all Leq values for a measurement in the form of sequence fragments.
     *
     * @param uuid Measurement unique identifier
     *
     * @return List of [LeqSequenceFragment]
     */
    suspend fun getLeqSequenceForMeasurement(uuid: String): List<LeqSequenceFragment>

    /**
     * Gets all location values for a measurement in the form of sequence fragments.
     *
     * @param uuid Measurement unique identifier
     *
     * @return List of [LocationSequenceFragment]
     */
    suspend fun getLocationSequenceForMeasurement(uuid: String): List<LocationSequenceFragment>

    /**
     * Starts a new ongoing measurement.
     * This will create a new mutable measurement object internally, then
     * Leq and location values can be pushed to this measurement as the
     * recording session goes.
     */
    fun openOngoingMeasurement()

    /**
     * Pushes a new [LeqSequenceFragment] to the current ongoing measurement.
     *
     * @param fragment Sequence fragment to be pushed.
     */
    suspend fun pushToOngoingMeasurement(fragment: LeqSequenceFragment)

    /**
     * Pushes a new [LocationSequenceFragment] to the current ongoing measurement.
     *
     * @param fragment Sequence fragment to be pushed.
     */
    suspend fun pushToOngoingMeasurement(fragment: LocationSequenceFragment)

    /**
     * Sets the recorded audio URL of the ongoing measurement.
     *
     * @param url Recorded audio file URL in local storage.
     */
    fun setOngoingMeasurementRecordedAudioUrl(url: String)

    /**
     * Ends the ongoing measurement and saves the result to local storage.
     */
    suspend fun endAndSaveOngoingMeasurement()
}
