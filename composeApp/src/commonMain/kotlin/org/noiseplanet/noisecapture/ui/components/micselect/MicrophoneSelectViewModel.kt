package org.noiseplanet.noisecapture.ui.components.micselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.MicrophoneInfo
import org.noiseplanet.noisecapture.services.audio.MicrophoneProviderService


class MicrophoneSelectViewModel : ViewModel(), KoinComponent {

    // - Properties

    private val microphoneProvider: MicrophoneProviderService by inject()

    val availableDevices = microphoneProvider.availableInputs
    val activeDevice = microphoneProvider.preferredInput


    // - Public functions

    fun selectMicrophone(microphoneInfo: MicrophoneInfo) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                microphoneProvider.setPreferredInput(microphoneInfo)
            }
        }
    }
}
