package org.noiseplanet.noisecapture.ui.features.calibration

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.components.micselect.MicrophoneSelectViewModel


val calibrationModule = module {

    viewModel {
        MicrophoneSelectViewModel()
    }
}
