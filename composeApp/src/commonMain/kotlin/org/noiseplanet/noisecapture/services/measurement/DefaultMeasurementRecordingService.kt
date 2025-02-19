package org.noiseplanet.noisecapture.services.measurement

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.AcousticIndicatorsData
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.Location
import org.noiseplanet.noisecapture.model.Measurement
import org.noiseplanet.noisecapture.services.audio.AudioRecordingService
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.location.UserLocationService
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import org.noiseplanet.noisecapture.services.settings.UserSettingsService
import org.noiseplanet.noisecapture.util.injectLogger
import kotlin.time.Duration.Companion.seconds


@OptIn(FormatStringsInDatetimeFormats::class)
open class DefaultMeasurementRecordingService : MeasurementRecordingService, KoinComponent {

    // - Constants

    private companion object {

        const val OUTPUT_FILE_DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss"
    }


    // - Properties

    private val measurementService: MeasurementService by inject()
    private val userLocationService: UserLocationService by inject()
    private val liveAudioService: LiveAudioService by inject()
    private val audioRecordingService: AudioRecordingService by inject()
    private val settingsService: UserSettingsService by inject()

    private val logger: Logger by injectLogger()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var recordingJob: Job? = null
    private var recordingLimitJob: Job? = null

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

        // Start live location updates
        userLocationService.startUpdatingLocation()
        // Start listening to measured acoustic indicators and location updates
        createMeasurementAndSubscribe()

        // Start recording audio to an output file, if enabled
        if (settingsService.get(SettingsKey.SettingSaveAudioWithMeasurement)) {
            val formattedDateTime = Clock.System.now().format(
                DateTimeComponents.Format { byUnicodePattern(OUTPUT_FILE_DATE_FORMAT) }
            )
            audioRecordingService.startRecordingToFile(outputFileName = "recording_$formattedDateTime")
            // Set listener to get output file URL
            audioRecordingService.recordingStopListener =
                object : AudioRecordingService.RecordingStopListener {
                    override fun onRecordingStop(fileUrl: String) {
                        // TODO: Attach this URL to the recorded measurement so audio can be played back later.
                        logger.debug("Recorded audio URL: $fileUrl")
                    }
                }

            // Schedule a job that will stop end the recording in N minutes
            // based on the limit fixed in settings
            recordingLimitJob = scope.launch {
                val maxDurationInSeconds =
                    settingsService.get(SettingsKey.SettingLimitSavedAudioDurationMinutes)// * 60u
                delay(maxDurationInSeconds.toLong().seconds.inWholeMilliseconds)
                audioRecordingService.stopRecordingToFile()
            }
        }
    }

    override fun endAndSave() {
        logger.debug("End recording")

        // Cancel running jobs
        recordingJob?.cancel()
        recordingJob = null
        recordingLimitJob?.cancel()
        recordingLimitJob = null

        _isRecording.tryEmit(false)

        // Stop live location updates
        userLocationService.stopUpdatingLocation()

        // End audio recording
        audioRecordingService.stopRecordingToFile()

        // Store measurement
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
        recordingJob = scope.launch {
            coroutineScope {
                launch {
                    userLocationService.liveLocation.collect { location ->
                        logger.debug("New location received: $location")
                        ongoingUserLocationHistory.add(location)
                    }
                }
                launch {
                    liveAudioService.getAcousticIndicatorsFlow().collect { indicators ->
                        ongoingAcousticIndicators.add(indicators)
                    }
                }
            }
        }
    }
}
