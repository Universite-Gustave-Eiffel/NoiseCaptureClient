package org.noiseplanet.noisecapture.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_mic_setup_calibrate_button
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.mic.MicrophoneInfo
import org.noiseplanet.noisecapture.audio.mic.MicrophoneProvider
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel


class HomeMicrophoneSetupViewModel : ViewModel(), KoinComponent {

    // - Properties

    private val microphoneProvider: MicrophoneProvider by inject()

    val availableDevices = microphoneProvider.availableInputs
    val activeDevice = microphoneProvider.preferredInput

    val calibrationButtonViewModel = NCButtonViewModel(
        title = Res.string.home_mic_setup_calibrate_button,
    )


    // - Public functions

    fun selectMicrophone(microphoneInfo: MicrophoneInfo) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                microphoneProvider.setPreferredInput(microphoneInfo)
            }
        }
    }
}
