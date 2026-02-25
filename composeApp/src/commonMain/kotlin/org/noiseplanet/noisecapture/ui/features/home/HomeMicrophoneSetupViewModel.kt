package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_mic_setup_calibrate_button
import noisecapture.composeapp.generated.resources.home_mic_setup_recalibrate_button
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.mic.MicrophoneProvider
import org.noiseplanet.noisecapture.model.dao.MicrophoneCalibrationProfile
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


class HomeMicrophoneSetupViewModel : ViewModel(), KoinComponent {

    // - View state

    data class ViewState(
        val buttonViewModel: NCButtonViewModel,
        val contentColor: Color,
        val containerColor: Color,
        val icon: ImageVector,
        val calibrationProfile: MicrophoneCalibrationProfile? = null,
    )


    // - Properties

    private val microphoneProvider: MicrophoneProvider by inject()

    val viewState: StateFlow<ViewState> = microphoneProvider.currentCalibrationProfile
        .map { calibrationProfile ->
            calibrationProfile?.let {
                ViewState(
                    buttonViewModel = NCButtonViewModel(
                        title = Res.string.home_mic_setup_recalibrate_button,
                        style = NCButtonStyle.OUTLINED,
                        colors = {
                            NCButtonColors(
                                containerColor = NoiseLevelColorRamp.level5Dark,
                                contentColor = NoiseLevelColorRamp.level5Dark
                            )
                        }
                    ),
                    contentColor = NoiseLevelColorRamp.level5Dark,
                    containerColor = NoiseLevelColorRamp.level5Light,
                    icon = Icons.Default.CheckCircleOutline,
                    calibrationProfile = it
                )
            } ?: ViewState(
                buttonViewModel = NCButtonViewModel(Res.string.home_mic_setup_calibrate_button),
                contentColor = NoiseLevelColorRamp.level6Dark,
                containerColor = NoiseLevelColorRamp.level6Light,
                icon = Icons.Default.Info,
            )
        }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = ViewState(
                buttonViewModel = NCButtonViewModel(Res.string.home_mic_setup_calibrate_button),
                contentColor = NoiseLevelColorRamp.level6Dark,
                containerColor = NoiseLevelColorRamp.level6Light,
                icon = Icons.Default.Info,
            )
        )
}
