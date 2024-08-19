package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.lifecycle.ViewModel
import org.noiseplanet.noisecapture.measurements.MeasurementsService

class MeasurementScreenViewModel(
    private val measurementsService: MeasurementsService,
) : ViewModel() {

    fun startRecordingAudio() = measurementsService.startRecordingAudio()

    fun stopRecordingAudio() = measurementsService.stopRecordingAudio()
}
