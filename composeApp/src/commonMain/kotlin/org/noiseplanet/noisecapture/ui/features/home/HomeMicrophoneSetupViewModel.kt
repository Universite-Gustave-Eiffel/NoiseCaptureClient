package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_mic_setup_calibrate_button
import noisecapture.composeapp.generated.resources.home_mic_setup_recalibrate_button
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.MicrophoneCalibrationProfile
import org.noiseplanet.noisecapture.services.audio.MicrophoneProviderService
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.theme.ColorSet
import org.noiseplanet.noisecapture.ui.theme.Noise
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


class HomeMicrophoneSetupViewModel : ViewModel(), KoinComponent {

    // - View state

    data class ViewState(
        val buttonContent: ButtonContent,
        val colors: ColorSet,
        val calibrationProfile: MicrophoneCalibrationProfile? = null,
    )


    // - Properties

    private val microphoneProvider: MicrophoneProviderService by inject()

    val viewState: StateFlow<ViewState> = microphoneProvider.currentCalibrationProfile
        .filterNotNull()
        .map { calibrationProfile ->
            if (calibrationProfile.isCalibrated) {
                ViewState(
                    buttonContent = ButtonContent(Res.string.home_mic_setup_recalibrate_button),
                    colors = Color.Noise.five,
                    calibrationProfile = calibrationProfile,
                )
            } else {
                ViewState(
                    colors = Color.Noise.six,
                    buttonContent = ButtonContent(Res.string.home_mic_setup_calibrate_button),
                    calibrationProfile = calibrationProfile,
                )
            }
        }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = ViewState(
                buttonContent = ButtonContent(Res.string.home_mic_setup_calibrate_button),
                colors = Color.Noise.six,
            )
        )
}
