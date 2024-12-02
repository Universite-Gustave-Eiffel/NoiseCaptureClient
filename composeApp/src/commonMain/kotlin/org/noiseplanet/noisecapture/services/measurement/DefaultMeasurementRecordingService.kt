package org.noiseplanet.noisecapture.services.measurement

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.audio.AcousticIndicatorsData
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.Location
import org.noiseplanet.noisecapture.model.Measurement
import org.noiseplanet.noisecapture.services.liveaudio.LiveAudioService
import org.noiseplanet.noisecapture.services.location.UserLocationService


class DefaultMeasurementRecordingService(
    private val measurementService: MeasurementService,
    private val userLocationService: UserLocationService,
    private val liveAudioService: LiveAudioService,
) : MeasurementRecordingService, KoinComponent {

    // - Constants

    companion object {

        private const val TAG = "RecordingService"
    }


    // - Properties

    private val logger: Logger by inject { parametersOf(TAG) }

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var recordingJob: Job? = null

    private val _isRecording = MutableStateFlow(value = false)

    // Stores the collected acoustic indicators and location data.
    // TODO: This is just a placeholder until a proper Measurement model is established
    //       in which case having a mutable measurement object would make more sense here
    //       rather than a series of individual properties
    private var ongoingUserLocationHistory: MutableList<Location> = mutableListOf()
    private var ongoingAcousticIndicators: MutableList<AcousticIndicatorsData> = mutableListOf()


    // - RecordingService

    override val isRecording: Boolean
        get() = _isRecording.value

    override val isRecordingFlow: StateFlow<Boolean>
        get() = _isRecording.asStateFlow()


    override fun start() {
        logger.debug("Start recording")
        _isRecording.tryEmit(true)

        createMeasurementAndSubscribe()
    }

    override fun endAndSave() {
        logger.debug("End recording")
        recordingJob?.cancel()
        recordingJob = null
        _isRecording.tryEmit(false)

        measurementService.storeMeasurement(
            Measurement(
                userLocationHistory = ongoingUserLocationHistory,
                acousticIndicators = ongoingAcousticIndicators,
            )
        )
        ongoingAcousticIndicators.clear()
        ongoingAcousticIndicators.clear()
    }


    // - Private functions

    /**
     * Creates a new ongoing measurement and subscribes to acoustic indicators and user
     * location flows to populate it during the recording session
     */
    private fun createMeasurementAndSubscribe() {
        // Clear any previously ongoing recording data
        ongoingAcousticIndicators.clear()
        ongoingUserLocationHistory.clear()
        recordingJob?.cancel()

        // Start listening to the various data sources during the recording session
        recordingJob = coroutineScope.launch {
            userLocationService.getLiveLocation().collect { location ->
                ongoingUserLocationHistory.add(location)
            }
            liveAudioService.getAcousticIndicatorsFlow().collect { acousticIndicators ->
                ongoingAcousticIndicators.add(acousticIndicators)
            }
        }
    }
}
