package org.noiseplanet.noisecapture.services.measurement

import Platform
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.measurement.Measurement
import org.noiseplanet.noisecapture.model.measurement.MutableMeasurement
import org.noiseplanet.noisecapture.util.injectLogger
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Default [MeasurementService] implementation
 */
@OptIn(ExperimentalUuidApi::class)
class DefaultMeasurementService : MeasurementService, KoinComponent {

    // - Properties

    private val platform: Platform by inject()
    private val logger: Logger by injectLogger()


    // - MeasurementService

    override fun storeMeasurement(mutableMeasurement: MutableMeasurement) {
        val now = Clock.System.now()
        val measurement = Measurement(
            uuid = Uuid.random().toString(),
            startedAt = mutableMeasurement.startedAt,
            endedAt = now,
            duration = now.minus(mutableMeasurement.startedAt),
            userAgent = platform.userAgent,
            locationSequence = mutableMeasurement.locationSequence,
            leqsSequence = mutableMeasurement.leqsSequence,
            recordedAudioUrl = mutableMeasurement.recordedAudioUrl
        )
        logger.debug("Storing measurement: ${measurement.uuid}")
        logger.debug("Leqs sequence length: ${mutableMeasurement.leqsSequence.size}")
        logger.debug("Location sequence length: ${mutableMeasurement.locationSequence.size}")

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
