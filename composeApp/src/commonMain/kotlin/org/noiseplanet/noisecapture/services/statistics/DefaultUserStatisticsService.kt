package org.noiseplanet.noisecapture.services.statistics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.model.dao.UserStatistics
import org.noiseplanet.noisecapture.services.storage.StorageService
import org.noiseplanet.noisecapture.services.storage.injectStorageService
import org.noiseplanet.noisecapture.util.injectLogger


/**
 * Default implementation for [UserStatisticsService].
 */
class DefaultUserStatisticsService : UserStatisticsService, KoinComponent {

    // - Properties

    private val statisticsStorageService: StorageService<UserStatistics> by injectStorageService()
    private val measurementStorageService: StorageService<Measurement> by injectStorageService()
    private val logger by injectLogger()

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())


    // - Lifecycle

    init {
        // When initializing this service, check that the currently stored definition matches
        // the number of actually stored measurements. If not, recalculate it from scratch.
        coroutineScope.launch {
            val measurementsCount = measurementStorageService.getIndex().size
            val currentStatistics = get()

            if (measurementsCount != currentStatistics.totalMeasurementsCount) {
                logger.warning(
                    "Found inconsistency between stored measurements count and measurements " +
                        "statistics count. This could be due to zombie measurements or measurement " +
                        "manually deleted (i.e. not by the app)."
                )
                logger.debug("Recalculating user statistics...")
                recalculate()
                logger.debug("Done!")
            }
        }
    }


    // - UserStatisticsService

    override suspend fun get(): UserStatistics {
        return statisticsStorageService.get(UserStatistics.UUID) ?: UserStatistics()
    }

    override fun getFlow(): Flow<UserStatistics> {
        return statisticsStorageService.subscribeOne(UserStatistics.UUID)
            .map { it ?: UserStatistics() }
    }

    override suspend fun addMeasurement(measurement: Measurement) {
        // Get currently stored value
        val currentValue = get()

        // Update statistics taking into account the given measurement
        statisticsStorageService.set(
            uuid = UserStatistics.UUID,
            newValue = currentValue.copy(
                totalMeasurementsCount = currentValue.totalMeasurementsCount + 1,
                totalMeasuredDuration = currentValue.totalMeasuredDuration + measurement.duration
            )
        )
    }

    override suspend fun removeMeasurement(measurement: Measurement) {
        // Get currently stored value
        val currentValue = get()

        // Update statistics taking into account the given measurement
        statisticsStorageService.set(
            uuid = UserStatistics.UUID,
            newValue = currentValue.copy(
                totalMeasurementsCount = currentValue.totalMeasurementsCount - 1,
                totalMeasuredDuration = currentValue.totalMeasuredDuration - measurement.duration
            )
        )
    }


    // - Private functions

    private suspend fun recalculate() {
        // Get all measurements and recalculate total duration
        val allMeasurements = measurementStorageService.getAll()
        val totalDuration = allMeasurements.map { it.duration }
            .reduceOrNull { total, duration ->
                total + duration
            } ?: 0

        // Update stored definition
        statisticsStorageService.set(
            uuid = UserStatistics.UUID,
            newValue = UserStatistics(
                totalMeasurementsCount = allMeasurements.size,
                totalMeasuredDuration = totalDuration
            )
        )
    }
}
