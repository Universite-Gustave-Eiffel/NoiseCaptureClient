package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.lifecycle.ViewModel
import org.noiseplanet.noisecapture.services.LiveRecordingService

class MeasurementScreenViewModel(
    private val liveRecordingService: LiveRecordingService,
) : ViewModel() {

    fun setupAudioSource() = liveRecordingService.setupAudioSource()

    fun startRecordingAudio() = liveRecordingService.startRecordingAudio()

    fun stopRecordingAudio() = liveRecordingService.stopRecordingAudio()

    fun releaseAudioSource() = liveRecordingService.releaseAudioSource()
}
