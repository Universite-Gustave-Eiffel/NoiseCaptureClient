package org.noiseplanet.noisecapture.ui.features.home

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {

    viewModel { (onClickSettingsButton: () -> Unit, onClickOpenSoundLevelMeterButton: () -> Unit) ->
        HomeScreenViewModel(onClickSettingsButton, onClickOpenSoundLevelMeterButton)
    }
}
