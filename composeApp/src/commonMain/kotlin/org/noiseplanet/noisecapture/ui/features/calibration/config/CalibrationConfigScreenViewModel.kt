package org.noiseplanet.noisecapture.ui.features.calibration.config

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.calibration_start_button_title
import noisecapture.composeapp.generated.resources.measurement_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.audio.MicrophoneProviderService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel


class CalibrationConfigScreenViewModel : ViewModel(), ScreenViewModel, KoinComponent {

    // - Properties

    private val microphoneProvider: MicrophoneProviderService by inject()

    override val title: StringResource = Res.string.measurement_title

    val startButtonViewModel = NCButtonViewModel(
        title = Res.string.calibration_start_button_title,
        icon = Icons.Default.Mic,
    )

    val currentCalibrationProfile = microphoneProvider.currentCalibrationProfile
}
