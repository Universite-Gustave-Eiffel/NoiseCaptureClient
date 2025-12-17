package org.noiseplanet.noisecapture.services.measurement

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.services.audio.AudioRecordingService
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.location.UserLocationService
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import org.noiseplanet.noisecapture.services.settings.UserSettingsService
import org.noiseplanet.noisecapture.util.injectLogger
import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime


@OptIn(FormatStringsInDatetimeFormats::class, ExperimentalTime::class)
open class DefaultRecordingService : RecordingService, KoinComponent {

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
    private var timerJob: Job? = null

    private val _isRecordingFlow = MutableStateFlow(value = false)
    private val _recordingDurationFlow = MutableStateFlow(value = Duration.ZERO)

    // Stores the collected acoustic indicators and location data.
    @Volatile
    private var currentLeqSequenceFragment: LeqSequenceFragment? = null

    @Volatile
    private var currentLocationSequenceFragment: LocationSequenceFragment? = null


    // - RecordingService

    override val isRecording: Boolean
        get() = _isRecordingFlow.value

    override val isRecordingFlow: StateFlow<Boolean>
        get() = _isRecordingFlow

    override val recordingDurationFlow: StateFlow<Duration>
        get() = _recordingDurationFlow

    override var onMeasurementDone: RecordingService.OnMeasurementDoneListener? = null


    override fun start() {
        logger.debug("Start recording")

        // Start live location updates
        userLocationService.startUpdatingLocation()
        // Start listening to measured acoustic indicators and location updates
        createMeasurementAndSubscribe()
        // Start timer
        startTimer()
        _isRecordingFlow.tryEmit(true)

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
                        logger.debug("Recorded audio URL: $fileUrl")
                        measurementService.setOngoingMeasurementRecordedAudioUrl(fileUrl)
                    }
                }

            // Schedule a job that will stop end the recording in N minutes
            // based on the limit fixed in settings
            recordingLimitJob = scope.launch {
                val maxDurationInSeconds =
                    settingsService.get(SettingsKey.SettingLimitSavedAudioDurationMinutes) * 60u
                delay(maxDurationInSeconds.toLong().seconds.inWholeMilliseconds)
                audioRecordingService.stopRecordingToFile()
            }
        }
    }

    override fun pause() {
        liveAudioService.stopListening()
    }

    override fun resume() {
        liveAudioService.startListening()
    }

    override fun endAndSave() {
        logger.debug("End recording")

        // Cancel running jobs
        recordingJob?.cancel()
        recordingJob = null
        recordingLimitJob?.cancel()
        recordingLimitJob = null

        _isRecordingFlow.tryEmit(false)

        // Stop live location updates
        userLocationService.stopUpdatingLocation()

        // End audio recording
        audioRecordingService.stopRecordingToFile()

        // Stop and reset timer
        stopTimer()
        _recordingDurationFlow.tryEmit(Duration.ZERO)

        measurementService.ongoingMeasurementUuid?.let { uuid ->
            // Store any uncompleted sequence fragment and store measurement
            scope.launch {
                measurementService.closeOngoingMeasurement()
                withContext(Dispatchers.Main) {
                    onMeasurementDone?.onDone(uuid)
                }
            }
        }
    }


    // - Private functions

    /**
     * Creates a new ongoing measurement and subscribes to acoustic indicators and user
     * location flows to populate it during the recording session
     */
    private fun createMeasurementAndSubscribe() {
        // Clear any previously ongoing recording data
        currentLeqSequenceFragment = null
        currentLocationSequenceFragment = null
        recordingJob?.cancel()

        // Open a new ongoing measurement in measurement service
        measurementService.openOngoingMeasurement()

        // Start listening to the various data sources during the recording session
        recordingJob = scope.launch {
            coroutineScope {
                // Subscribe to location updates
                launch {
                    userLocationService.liveLocation.collect { location ->
                        logger.debug("New location received: $location")
                        measurementService.pushToOngoingMeasurement(location)
                    }
                }

                // Subscribe to noise level updates
                launch {
                    liveAudioService.getLeqRecordsFlow().collect {
                        measurementService.pushToOngoingMeasurement(it)
                    }
                }

                // Subscribe to audio service state updates
                launch {
                    liveAudioService.isRunningFlow.collect { isRunning ->
                        if (!isRecording) return@collect

                        // If a recording session is running, when audio source pauses or resumes,
                        // propagate the state change to the recording timer.
                        if (isRunning) {
                            startTimer()
                        } else {
                            stopTimer()
                        }
                    }
                }
            }
        }
    }

    private fun startTimer() {
        if (timerJob != null) return

        timerJob = scope.launch {
            while (isActive) {
                delay(250.milliseconds)
                _recordingDurationFlow.tryEmit(_recordingDurationFlow.value + 250.milliseconds)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
}
