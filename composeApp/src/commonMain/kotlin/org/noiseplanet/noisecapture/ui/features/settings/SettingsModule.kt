package org.noiseplanet.noisecapture.ui.features.settings

import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {

    viewModel {
        SettingsScreenViewModel(
            settingsService = get()
        )
    }
}
