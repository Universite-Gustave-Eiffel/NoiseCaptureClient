package org.noiseplanet.noisecapture.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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


interface RecordingService {

    /**
     * True if the service is currently recording incoming data, false otherwise
     */
    val isRecording: Boolean

    /**
     * A flow of [isRecording] values
     */
    val isRecordingFlow: StateFlow<Boolean>

    /**
     * Starts a new recording of acoustic parameters and location updates
     */
    fun start()

    /**
     * Ends the current recording and saves the results to the app's storage
     */
    fun endAndSave()
}


class DefaultRecordingService(
    private val measurementService: MeasurementService,
    private val userLocationService: UserLocationService,
    private val liveAudioService: LiveAudioService,
) : RecordingService, KoinComponent {

    // - Constants

    companion object {

        private const val TAG = "RecordingService"
    }


    // - Properties

    private val logger: Logger by inject { parametersOf(TAG) }

    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    private val _isRecording = MutableStateFlow(value = false)

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
        job.cancel()
        _isRecording.tryEmit(false)

        measurementService.storeMeasurement(
            Measurement(
                userLocationHistory = ongoingUserLocationHistory,
                acousticIndicators = ongoingAcousticIndicators,
            )
        )
    }


    // - Private functions

    /**
     * Creates a new ongoing measurement and subscribes to acoustic indicators and user
     * location flows to populate it during the recording session
     */
    private fun createMeasurementAndSubscribe() {
        coroutineScope.launch {
            userLocationService.getLiveLocation().collect { location ->
                ongoingUserLocationHistory.add(location)
            }
            liveAudioService.getAcousticIndicatorsFlow().collect { acousticIndicators ->
                ongoingAcousticIndicators.add(acousticIndicators)
            }
        }
    }
}
