package org.noiseplanet.noisecapture.ui.features.recording

import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.measurement.RecordingService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel

class RecordingScreenViewModel : ViewModel(), ScreenViewModel, KoinComponent {

    // - Properties

    private val recordingService: RecordingService by inject()

    var showEndRecordingConfirmationDialog: (() -> Unit)? = null

    /**
     * If true, will navigate to details screen once measurement is done.
     * Will be set to false if measurement ends because user leaves the measurement screen.
     */
    var shouldOpenDetailsOnceDone: Boolean = true


    // - ScreenViewModel

    override val title: StringResource
        get() = Res.string.measurement_title

    override val confirmPopBackStack: () -> Boolean
        get() = {
            if (recordingService.isRecording) {
                shouldOpenDetailsOnceDone = false
                showEndRecordingConfirmationDialog?.let { it() }
                false
            } else {
                true
            }
        }


    // - Public functions

    fun endCurrentRecording() {
        recordingService.endAndSave()
    }

    fun registerMeasurementDoneListener(onMeasurementDone: (String) -> Unit) {
        recordingService.onMeasurementDone =
            object : RecordingService.OnMeasurementDoneListener {
                override fun onDone(measurementUuid: String) {
                    onMeasurementDone(measurementUuid)
                }
            }
    }

    fun deregisterMeasurementDoneListener() {
        recordingService.onMeasurementDone = null
    }
}
