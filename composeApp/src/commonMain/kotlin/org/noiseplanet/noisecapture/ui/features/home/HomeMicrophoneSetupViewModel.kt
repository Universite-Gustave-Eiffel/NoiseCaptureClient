package org.noiseplanet.noisecapture.ui.features.home

import androidx.lifecycle.ViewModel
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

    val availableDevices = microphoneProvider.availableDevices
    val activeDevice = microphoneProvider.activeDevice

    val calibrationButtonViewModel = NCButtonViewModel(
        title = Res.string.home_mic_setup_calibrate_button,
    )


    // - Public functions

    fun selectMicrophone(microphoneInfo: MicrophoneInfo) {
        microphoneProvider.selectDevice(microphoneInfo)
    }
}
