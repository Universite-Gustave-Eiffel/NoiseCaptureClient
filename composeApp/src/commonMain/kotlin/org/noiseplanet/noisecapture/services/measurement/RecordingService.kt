package org.noiseplanet.noisecapture.services.measurement

import kotlinx.coroutines.flow.StateFlow


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
     * Called when ending a measurement, after it has been stored locally.
     */
    var onMeasurementDone: OnMeasurementDoneListener?


    // - Public functions

    /**
     * Starts a new recording of acoustic parameters and location updates
     */
    fun start()

    /**
     * Ends the current recording and saves the results to the app's storage
     */
    fun endAndSave()
}
