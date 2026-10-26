package org.noiseplanet.noisecapture.ui.features.recording

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_body
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_confirm
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_continue
import noisecapture.composeapp.generated.resources.measurement_end_confirmation_dialog_title
import noisecapture.composeapp.generated.resources.measurement_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.location.UserLocationService
import org.noiseplanet.noisecapture.services.measurement.RecordingService
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.components.dialog.NCDialogViewModel
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.components.tertiaryContainerColors
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise

class RecordingScreenViewModel : ViewModel(), ScreenViewModel, KoinComponent {

    // - Properties

    private val recordingService: RecordingService by inject()
    private val locationService: UserLocationService by inject()

    var showEndRecordingConfirmationDialog: (() -> Unit)? = null

    /**
     * If true, will navigate to details screen once measurement is done.
     * Will be set to false if measurement ends because user leaves the measurement screen.
     */
    var shouldOpenDetailsOnceDone: Boolean = true

    val isLocationAvailable = locationService.isLocationAvailable


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

    val endRecordingConfirmationDialogViewModel = NCDialogViewModel(
        containerColors = Color.Noise.two.secondaryContainerColors(),
        confirmButtonContent = ButtonContent(title = Res.string.measurement_end_confirmation_dialog_confirm),
        confirmButtonColors = Color.Noise.two.tertiaryContainerColors(hasDropShadow = true),
        dismissButtonContent = ButtonContent(title = Res.string.measurement_end_confirmation_dialog_continue),
        dismissButtonColors = Color.Noise.two.transparentContainerColors(),
        title = Res.string.measurement_end_confirmation_dialog_title,
        text = Res.string.measurement_end_confirmation_dialog_body,
    )


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
