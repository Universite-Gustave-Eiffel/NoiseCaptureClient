package org.noiseplanet.noisecapture.services.measurement

import Platform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.LAeqMetrics
import org.noiseplanet.noisecapture.model.dao.LeqRecord
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationRecord
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.model.dao.MeasurementSummary
import org.noiseplanet.noisecapture.model.dao.MutableMeasurement
import org.noiseplanet.noisecapture.services.storage.StorageService
import org.noiseplanet.noisecapture.services.storage.injectStorageService
import org.noiseplanet.noisecapture.util.injectLogger
import org.noiseplanet.noisecapture.util.isInVuMeterRange
import org.noiseplanet.noisecapture.util.roundTo
import kotlin.concurrent.Volatile
import kotlin.math.max
import kotlin.math.min
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Default implementation for [MeasurementService].
 */
@Suppress("TooManyFunctions")
class DefaultMeasurementService : MeasurementService, KoinComponent {

    // - Constants

    private companion object {

        // Sets the maximum number of records that can be in a sequence fragment. Whenever a
        // sequence fragment reaches this limit, it gets stored and a new fragment is created.
        // 250 records is roughly 30 seconds of data coming at 125ms interval.
        const val SEQUENCE_FRAGMENT_MAX_SIZE: Int = 250
    }


    // - Properties

    private val laeqMetricsFlow: MutableStateFlow<LAeqMetrics?> = MutableStateFlow(null)

    private val logger: Logger by injectLogger()
    private val platform: Platform by inject()

    private val measurementStorageService: StorageService<Measurement> by injectStorageService()
    private val leqSequenceStorageService: StorageService<LeqSequenceFragment> by injectStorageService()
    private val locationSequenceStorageService: StorageService<LocationSequenceFragment> by injectStorageService()

    private var ongoingMeasurement: MutableMeasurement? = null

    @Volatile // <- Ensures thread safety
    private var currentLeqSequenceFragment: LeqSequenceFragment? = null

    @Volatile // <- Ensures thread safety
    private var currentLocationSequenceFragment: LocationSequenceFragment? = null


    // - MeasurementService

    override val ongoingMeasurementUuid: String?
        get() = ongoingMeasurement?.uuid

    override suspend fun getAllMeasurements(): List<Measurement> {
        return measurementStorageService.getAll()
    }

    override fun getAllMeasurementsFlow(): Flow<List<Measurement>> {
        return measurementStorageService.subscribeAll()
    }

    override suspend fun getMeasurement(uuid: String): Measurement? {
        return measurementStorageService.get(uuid)
    }

    override fun getMeasurementFlow(uuid: String): Flow<Measurement?> {
        return measurementStorageService.subscribeOne(uuid)
    }

    override suspend fun getLeqSequenceForMeasurement(uuid: String): List<LeqSequenceFragment> {
        val measurement = getMeasurement(uuid) ?: return emptyList()

        return measurement.leqsSequenceIds.mapNotNull {
            leqSequenceStorageService.get(it)
        }
    }

    override suspend fun getLocationSequenceForMeasurement(uuid: String): List<LocationSequenceFragment> {
        val measurement = getMeasurement(uuid) ?: return emptyList()

        return measurement.locationSequenceIds.mapNotNull {
            locationSequenceStorageService.get(it)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun openOngoingMeasurement() {
        // Create a new ongoing measurement with a unique identifier and start time to now.
        ongoingMeasurement = MutableMeasurement(
            uuid = Uuid.random().toString(),
            startTimestamp = Clock.System.now().toEpochMilliseconds()
        )
        logger.info("Starting new measurement with id ${ongoingMeasurement?.uuid}")
    }

    override fun getOngoingMeasurementLaeqMetricsFlow(): Flow<LAeqMetrics?> {
        return laeqMetricsFlow
    }

    override suspend fun pushToOngoingMeasurement(record: LeqRecord) {
        val ongoingMeasurement = ongoingMeasurement ?: return
        // If we already have an ongoing sequence, simply push the new
        // record to the list
        currentLeqSequenceFragment?.apply {
            push(record)
        } ?: run {
            // Otherwise, create a new sequence before pushing the new record
            currentLeqSequenceFragment = LeqSequenceFragment(
                measurementId = ongoingMeasurement.uuid,
                index = ongoingMeasurement.leqsSequenceIds.size
            )
            currentLeqSequenceFragment?.push(record)
        }
        if (record.laeq.isInVuMeterRange()) {
            // Update (or initialize) ongoing measurement's leq metrics
            val laeqMetrics = ongoingMeasurement.laeqMetrics?.let { currentMetrics ->
                val average = currentMetrics.average +
                    (record.laeq - currentMetrics.average) / currentMetrics.recordsCount
                LAeqMetrics(
                    min = min(record.laeq, currentMetrics.min),
                    average = average.roundTo(1),
                    max = max(record.laeq, currentMetrics.max),
                    recordsCount = currentMetrics.recordsCount + 1
                )
            } ?: LAeqMetrics(
                min = record.laeq,
                average = record.laeq,
                max = record.laeq,
                recordsCount = 1,
            )
            ongoingMeasurement.laeqMetrics = laeqMetrics
            laeqMetricsFlow.emit(laeqMetrics)
        }
        // Check if sequence fragment has reached its limit.
        // Since location updates come at an irregular rate, we rely on leq records to determine
        // when fragments should stop.
        val fragmentSize = currentLeqSequenceFragment?.size ?: return
        if (fragmentSize >= SEQUENCE_FRAGMENT_MAX_SIZE) {
            onSequenceFragmentEnd()
        }
    }

    override suspend fun pushToOngoingMeasurement(record: LocationRecord) {
        val ongoingMeasurement = ongoingMeasurement ?: return
        // If we already have an ongoing sequence, simply push the new
        // record to the list
        currentLocationSequenceFragment?.apply {
            push(record)
        } ?: run {
            // Otherwise, create a new sequence before pushing the new record
            currentLocationSequenceFragment = LocationSequenceFragment(
                measurementId = ongoingMeasurement.uuid,
                index = ongoingMeasurement.locationSequenceIds.size
            )
            currentLocationSequenceFragment?.push(record)
        }
    }

    override fun setOngoingMeasurementRecordedAudioUrl(url: String) {
        ongoingMeasurement?.recordedAudioUrl = url
    }

    override suspend fun closeOngoingMeasurement() {
        // End currently ongoing sequence fragments
        onSequenceFragmentEnd()

        // Compute measurement summary metrics
        val measurement = ongoingMeasurement ?: return
        val allMeasurementLeqSorted: List<Double> = measurement.leqsSequenceIds
            .fold(listOf<Double>()) { accumulator, sequenceId ->
                accumulator + (leqSequenceStorageService.get(sequenceId)?.laeq ?: emptyList())
            }
            .filter { it.isInVuMeterRange() }
            .sorted()
        val summary = MeasurementSummary(
            la10 = allMeasurementLeqSorted[(allMeasurementLeqSorted.size / 100.0 * 90.0).toInt()],
            la50 = allMeasurementLeqSorted[(allMeasurementLeqSorted.size / 100.0 * 50.0).toInt()],
            la90 = allMeasurementLeqSorted[(allMeasurementLeqSorted.size / 100.0 * 10.0).toInt()],
        )
        saveOngoingMeasurement(summary)
    }

    override suspend fun deleteMeasurement(uuid: String) {
        measurementStorageService.delete(uuid)
    }


    // - Private functions

    /**
     * Called every N seconds to end current sequence fragment,
     * store values and start a new fragment.
     */
    private suspend fun onSequenceFragmentEnd() {
        // Store values to local storage and add id to measurement
        currentLocationSequenceFragment?.let {
            locationSequenceStorageService.set(it.uuid, it)
            ongoingMeasurement?.locationSequenceIds?.add(it.uuid)
        }
        currentLeqSequenceFragment?.let {
            leqSequenceStorageService.set(it.uuid, it)
            ongoingMeasurement?.leqsSequenceIds?.add(it.uuid)
        }
        // Save ongoing measurement up until now, in case it gets interrupted later on.
        saveOngoingMeasurement()
        // Clear current fragments so that next time a new record is pushed, new fragments are created
        currentLeqSequenceFragment = null
        currentLocationSequenceFragment = null
    }

    /**
     * Saves ongoing measurement at a given point in time.
     */
    private suspend fun saveOngoingMeasurement(summary: MeasurementSummary? = null) {
        val ongoingMeasurement = ongoingMeasurement ?: return
        val leqMetrics = ongoingMeasurement.laeqMetrics ?: return
        val now = Clock.System.now().toEpochMilliseconds()

        logger.info("Storing measurement with id ${ongoingMeasurement.uuid}")

        // Create definitive measurement object from the mutable one
        val measurement = Measurement(
            uuid = ongoingMeasurement.uuid,
            startTimestamp = ongoingMeasurement.startTimestamp,
            endTimestamp = now,
            duration = now - ongoingMeasurement.startTimestamp,
            userAgent = platform.userAgent,
            locationSequenceIds = ongoingMeasurement.locationSequenceIds,
            leqsSequenceIds = ongoingMeasurement.leqsSequenceIds,
            recordedAudioUrl = ongoingMeasurement.recordedAudioUrl,
            laeqMetrics = leqMetrics,
            summary = summary,
        )
        measurementStorageService.set(measurement.uuid, measurement)
        laeqMetricsFlow.emit(null)
    }
}
