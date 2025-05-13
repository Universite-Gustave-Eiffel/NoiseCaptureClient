package org.noiseplanet.noisecapture.ui.features.home

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {

    viewModel { params ->
        HomeScreenViewModel(
            onClickSettingsButton = params.get(),
            onClickOpenSoundLevelMeterButton = params.get(),
            onClickMeasurement = params.get(),
            onClickOpenHistoryButton = params.get(),
        )
    }
}
