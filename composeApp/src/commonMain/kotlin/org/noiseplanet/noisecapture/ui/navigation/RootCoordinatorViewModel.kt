package org.noiseplanet.noisecapture.ui.navigation

import androidx.lifecycle.ViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.liveaudio.LiveAudioService
import org.noiseplanet.noisecapture.services.measurement.MeasurementRecordingService

class RootCoordinatorViewModel : ViewModel(), KoinComponent {

    // - Properties

    private val liveAudioService: LiveAudioService by inject()
    private val measurementRecordingService: MeasurementRecordingService by inject()

    val isRecording: Boolean
        get() = measurementRecordingService.isRecording


    // - Public functions

    fun setupAudioSource() = liveAudioService.setupAudioSource()
    fun releaseAudioSource() = liveAudioService.releaseAudioSource()

    /**
     * If there is no ongoing measurement recording, resume audio source
     */
    fun startAudioSourceIfNotRecording() {
        if (!isRecording) {
            liveAudioService.startListening()
        }
    }

    /**
     * If there is no ongoing measurement recording, pause audio source
     */
    fun stopAudioSourceIfNotRecording() {
        if (!isRecording) {
            liveAudioService.stopListening()
        }
    }

    fun endRecording() = measurementRecordingService.endAndSave()

    /**
     * Given a route name, checks if the corresponding screen uses audio source.
     * If true, resume audio source if there is no ongoing measurement.
     * If false, pause audio source if there is no ongoing measurement.
     */
    fun toggleAudioSourceForScreen(routeName: String) {
        Route.entries.firstOrNull {
            it.name == routeName
        }?.let {
            if (it.usesAudioSource) {
                startAudioSourceIfNotRecording()
            } else {
                stopAudioSourceIfNotRecording()
            }
        }
    }
}
