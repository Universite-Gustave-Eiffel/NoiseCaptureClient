package org.noiseplanet.noisecapture.ui.features.home

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel

val homeModule = module {

    viewModel {
        LastMeasurementsViewModel()
    }

    viewModel {
        SoundLevelMeterViewModel(
            showMinMaxSPL = false,
            showPlayPauseButton = true,
        )
    }
}
