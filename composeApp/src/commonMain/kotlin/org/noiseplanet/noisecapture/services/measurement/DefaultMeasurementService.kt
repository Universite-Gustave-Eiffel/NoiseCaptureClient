package org.noiseplanet.noisecapture.services.measurement

import Platform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.LeqRecord
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationRecord
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.model.dao.MutableMeasurement
import org.noiseplanet.noisecapture.services.storage.StorageService
import org.noiseplanet.noisecapture.services.storage.injectStorageService
import org.noiseplanet.noisecapture.util.injectLogger
import kotlin.concurrent.Volatile
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Default implementation for [MeasurementService].
 */
@Suppress("TooManyFunctions")
class DefaultMeasurementService : MeasurementService, KoinComponent {

    // - Constants

    private companion object {

        // Duration of a location or leq sequence fragment. Every time this amount of time elapses,
        // sequence values will be stored in filesystem and a new sequence fragment will be created.
        // That way, a very long measurement won't infinitely take up more and more RAM space.
        const val SEQUENCE_FRAGMENT_DURATION_MILLISECONDS: Long = 30_000 // 30 seconds
    }


    // - Properties

    private val logger: Logger by injectLogger()
    private val platform: Platform by inject()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var sequenceFragmentationJob: Job? = null

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

    override suspend fun getMeasurement(uuid: String): Measurement? {
        return measurementStorageService.get(uuid)
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

        // Schedule another job that will store current sequence fragment every n seconds
        // and add the stored sequence id to the ongoing measurement.
        sequenceFragmentationJob = scope.launch {
            while (isActive) {
                delay(SEQUENCE_FRAGMENT_DURATION_MILLISECONDS)
                onSequenceFragmentEnd()
            }
        }
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
        // End currently ongoing sequence fragments and save measurement data
        onSequenceFragmentEnd()
        // Cancel sequence fragmentation job
        sequenceFragmentationJob?.cancel()
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
    private suspend fun saveOngoingMeasurement() {
        val ongoingMeasurement = ongoingMeasurement ?: return
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
            recordedAudioUrl = ongoingMeasurement.recordedAudioUrl
        )
        measurementStorageService.set(measurement.uuid, measurement)
    }
}
