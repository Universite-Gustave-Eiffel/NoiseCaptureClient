package org.noiseplanet.noisecapture.services.statistics

import kotlinx.coroutines.flow.Flow
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.model.dao.UserStatistics

/**
 * Read and update user statistics (number of measurements, total duration, etc)
 */
interface UserStatisticsService {

    // - Public functions

    /**
     * Get the currently stored statistics values.
     *
     * @return Currently stored statistics values.
     */
    suspend fun get(): UserStatistics

    /**
     * Get a [Flow] of statistics values, emitting a new value on every update.
     *
     * @return A [Flow] of statistics values
     */
    fun getFlow(): Flow<UserStatistics>

    /**
     * Adds the given measurement to the statistics. I.e. update the currently stored statistics
     * by taking into account this measurement as well.
     *
     * @param measurement Measurement to add.
     */
    suspend fun addMeasurement(measurement: Measurement)

    /**
     * Removes the given measurement from the statistics. I.e. update the currently stored statistics
     * by taking into account this measurement as well.
     *
     * @param measurement Measurement to remove.
     */
    suspend fun removeMeasurement(measurement: Measurement)
}
