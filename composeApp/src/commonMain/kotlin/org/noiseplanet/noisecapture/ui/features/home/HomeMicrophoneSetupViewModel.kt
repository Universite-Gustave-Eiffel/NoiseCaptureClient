package org.noiseplanet.noisecapture.ui.features.home

import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_mic_setup_calibrate_button
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel


class HomeMicrophoneSetupViewModel : ViewModel() {

    // - Properties

    val calibrationButtonViewModel = NCButtonViewModel(
        title = Res.string.home_mic_setup_calibrate_button,
    )
}
