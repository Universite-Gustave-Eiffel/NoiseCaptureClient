package org.noiseplanet.noisecapture.services.measurement

import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration


interface RecordingService {

    // - Listeners

    /**
     * Called when ending a measurement, after it has been stored locally.
     */
    interface OnMeasurementDoneListener {

        fun onDone(measurementUuid: String)
    }


    // - Properties

    /**
     * True if the service is currently recording incoming data, false otherwise
     */
    val isRecording: Boolean

    /**
     * A flow of [isRecording] values
     */
    val isRecordingFlow: StateFlow<Boolean>

    /**
     * Emits the duration of the current recording at regular intervals.
     * Equal to [Duration.ZERO] when no recording is currently ongoing.
     */
    val recordingDurationFlow: StateFlow<Duration>

    /**
     * Called when ending a measurement, after it has been stored locally.
     */
    var onMeasurementDone: OnMeasurementDoneListener?


    // - Public functions

    /**
     * Starts a new recording of acoustic parameters and location updates.
     */
    fun start()

    /**
     * Pause current recording, suspending timer and analysis of incoming audio.
     */
    fun pause()

    /**
     * Resumes current recording, resuming timer and analysis of incoming audio.
     */
    fun resume()

    /**
     * Ends the current recording and saves the results to the app's storage
     */
    fun endAndSave()
}
