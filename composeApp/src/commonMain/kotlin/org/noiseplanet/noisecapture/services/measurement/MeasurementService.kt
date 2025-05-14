package org.noiseplanet.noisecapture.services.measurement

import kotlinx.coroutines.flow.Flow
import org.noiseplanet.noisecapture.model.dao.LAeqMetrics
import org.noiseplanet.noisecapture.model.dao.LeqRecord
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationRecord
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.model.dao.Measurement


/**
 * Interact with measurements using various storage services for the different files
 * that constitute a measurement (measurement object, leq sequence, location sequence, etc)
 */
@Suppress("TooManyFunctions")
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
     * Gets a flow of all measurements, emitting a new value everytime a measurement is
     * created or deleted.
     *
     * Note: A new value won't be emitted if an existing measurement is updated.
     *
     * @return A [Flow] of all measurements in storage.
     */
    fun getAllMeasurementsFlow(): Flow<List<Measurement>>

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
     * Subscribe to every updates made a particular measurement.
     *
     * @param uuid Measurement unique identifier
     *
     * @return A [Flow] of measurement values, null if there is no measurement for the
     *         given identifier.
     */
    fun getMeasurementFlow(uuid: String): Flow<Measurement?>

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
     * Gets a flow of leq metrics (min/max/average) for the ongoing measurement, or
     * null if no measurement is currently running.
     *
     * @return Ongoing measurement leq metrics updated in real time.
     */
    fun getOngoingMeasurementLaeqMetricsFlow(): Flow<LAeqMetrics?>

    /**
     * Pushes a new [LeqRecord] to the current ongoing measurement.
     *
     * @param record Record to be pushed.
     */
    suspend fun pushToOngoingMeasurement(record: LeqRecord)

    /**
     * Pushes a new [LocationRecord] to the current ongoing measurement.
     *
     * @param record Record to be pushed.
     */
    suspend fun pushToOngoingMeasurement(record: LocationRecord)

    /**
     * Sets the recorded audio URL of the ongoing measurement.
     *
     * @param url Recorded audio file URL in local storage.
     */
    fun setOngoingMeasurementRecordedAudioUrl(url: String)

    /**
     * Closes the ongoing measurement, saving every remaining data to local storage.
     */
    suspend fun closeOngoingMeasurement()

    /**
     * Deletes the measurement with the given id.
     *
     * @param uuid Measurement unique identifier.
     */
    suspend fun deleteMeasurement(uuid: String)
}
