package org.noiseplanet.noisecapture.services.measurement

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.model.dao.LeqRecord
import org.noiseplanet.noisecapture.model.dao.LeqSequenceFragment
import org.noiseplanet.noisecapture.model.dao.LocationRecord
import org.noiseplanet.noisecapture.model.dao.LocationSequenceFragment
import org.noiseplanet.noisecapture.services.audio.AudioRecordingService
import org.noiseplanet.noisecapture.services.audio.LiveAudioService
import org.noiseplanet.noisecapture.services.location.UserLocationService
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import org.noiseplanet.noisecapture.services.settings.UserSettingsService
import org.noiseplanet.noisecapture.util.injectLogger
import kotlin.concurrent.Volatile
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi


@OptIn(FormatStringsInDatetimeFormats::class)
open class DefaultMeasurementRecordingService : MeasurementRecordingService, KoinComponent {

    // - Constants

    private companion object {

        const val OUTPUT_FILE_DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss"

        // Duration of a location or leq sequence fragment. Every time this amount of time elapses,
        // sequence values will be stored in filesystem and a new sequence fragment will be created.
        // That way, a very long measurement won't infinitely take up more and more RAM space.
        const val SEQUENCE_FRAGMENT_DURATION_MILLISECONDS: Long = 30_000 // 30 seconds
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
    @Volatile
    private var currentLeqSequenceFragment: LeqSequenceFragment? = null

    @Volatile
    private var currentLocationSequenceFragment: LocationSequenceFragment? = null


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

        // Store result
        scope.launch {
            onSequenceFragmentEnd()
            measurementService.endAndSaveOngoingMeasurement()
        }
    }


    // - Private functions

    /**
     * Creates a new ongoing measurement and subscribes to acoustic indicators and user
     * location flows to populate it during the recording session
     */
    @OptIn(ExperimentalUuidApi::class)
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
                        onNewLocationRecord(location)
                    }
                }

                // Subscribe to
                launch {
                    liveAudioService.getAcousticIndicatorsFlow().collect { indicators ->
                        // TODO: This could be done already in LiveAudioService?
                        // Map acoustic indicators to LeqRecord
                        val leqRecord = LeqRecord(
                            // TODO: Properly fill LZeq and LCeq
                            timestamp = Clock.System.now().toEpochMilliseconds(),
                            lzeq = indicators.laeq,
                            laeq = indicators.laeq,
                            lceq = indicators.laeq,
                            leqsPerThirdOctave = indicators.leqsPerThirdOctave
                        )
                        onNewLeqRecord(leqRecord)
                    }
                }

                // Schedule another job that will store current sequence fragment every n seconds
                // and add the stored sequence id to the ongoing measurement.
                launch {
                    while (isActive) {
                        delay(SEQUENCE_FRAGMENT_DURATION_MILLISECONDS)
                        onSequenceFragmentEnd()
                    }
                }
            }
        }
    }

    /**
     * Handles a new incoming location record.
     */
    private fun onNewLocationRecord(record: LocationRecord) {
        val ongoingMeasurementUuid = measurementService.ongoingMeasurementUuid ?: return
        // If we already have an ongoing sequence, simply push the new
        // record to the list
        currentLocationSequenceFragment?.apply {
            push(record)
        } ?: run {
            // Otherwise, create a new sequence before pushing the new record
            currentLocationSequenceFragment =
                LocationSequenceFragment(measurementId = ongoingMeasurementUuid)
            currentLocationSequenceFragment?.push(record)
        }
    }

    /**
     * Handles a new incoming Leq record.
     */
    private fun onNewLeqRecord(record: LeqRecord) {
        val ongoingMeasurementUuid = measurementService.ongoingMeasurementUuid ?: return
        // If we already have an ongoing sequence, simply push the new
        // record to the list
        currentLeqSequenceFragment?.apply {
            push(record)
        } ?: run {
            // Otherwise, create a new sequence before pushing the new record
            currentLeqSequenceFragment =
                LeqSequenceFragment(measurementId = ongoingMeasurementUuid)
            currentLeqSequenceFragment?.push(record)
        }
    }

    /**
     * Called every N seconds to end current sequence fragment,
     * store values and start a new fragment.
     */
    private suspend fun onSequenceFragmentEnd() {
        // Store values to local storage and add id to measurement
        currentLocationSequenceFragment?.let {
            measurementService.pushToOngoingMeasurement(it)
        }
        currentLeqSequenceFragment?.let {
            measurementService.pushToOngoingMeasurement(it)
        }
        currentLeqSequenceFragment = null
        currentLocationSequenceFragment = null
    }
}
