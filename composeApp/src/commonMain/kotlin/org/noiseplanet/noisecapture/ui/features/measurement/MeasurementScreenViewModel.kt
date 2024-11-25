package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.lifecycle.ViewModel
import org.noiseplanet.noisecapture.services.LiveAudioService

class MeasurementScreenViewModel(
    private val liveAudioService: LiveAudioService,
) : ViewModel() {

    fun setupAudioSource() = liveAudioService.setupAudioSource()

    fun startRecordingAudio() = liveAudioService.startListening()

    fun stopRecordingAudio() = liveAudioService.stopListening()

    fun releaseAudioSource() = liveAudioService.releaseAudioSource()
}
