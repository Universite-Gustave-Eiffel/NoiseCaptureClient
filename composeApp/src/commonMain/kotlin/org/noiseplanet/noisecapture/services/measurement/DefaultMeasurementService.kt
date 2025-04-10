package org.noiseplanet.noisecapture.services.measurement

import Platform
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.model.dao.MutableMeasurement
import org.noiseplanet.noisecapture.services.storage.StorageService
import org.noiseplanet.noisecapture.services.storage.injectStorageService
import org.noiseplanet.noisecapture.util.injectLogger
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Default implementation for [MeasurementService].
 */
class DefaultMeasurementService : MeasurementService, KoinComponent {

    // - Properties

    private val logger: Logger by injectLogger()
    private val platform: Platform by inject()

    private val measurementStorageService: StorageService<Measurement> by injectStorageService()
    private val leqSequenceStorageService: StorageService<LeqSequenceFragment> by injectStorageService()
    private val locationSequenceStorageService: StorageService<LocationSequenceFragment> by injectStorageService()

    private var ongoingMeasurement: MutableMeasurement? = null


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
    }

    override suspend fun pushToOngoingMeasurement(fragment: LeqSequenceFragment) {
        leqSequenceStorageService.set(fragment.uuid, fragment)
        ongoingMeasurement?.leqsSequenceIds?.add(fragment.uuid)
    }

    override suspend fun pushToOngoingMeasurement(fragment: LocationSequenceFragment) {
        locationSequenceStorageService.set(fragment.uuid, fragment)
        ongoingMeasurement?.locationSequenceIds?.add(fragment.uuid)
    }

    override fun setOngoingMeasurementRecordedAudioUrl(url: String) {
        ongoingMeasurement?.recordedAudioUrl = url
    }

    override suspend fun endAndSaveOngoingMeasurement() {
        val ongoingMeasurement = ongoingMeasurement ?: return
        val now = Clock.System.now().toEpochMilliseconds()

        logger.info("Ending and storing measurement with id ${ongoingMeasurement.uuid}")

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
