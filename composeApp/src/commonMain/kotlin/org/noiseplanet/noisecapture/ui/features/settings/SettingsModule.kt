package org.noiseplanet.noisecapture.ui.features.settings

import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.features.settings.about.AboutScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.help.HelpScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.microphone.MicrophoneSettingsScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.privacy.PrivacySettingsScreenViewModel

val settingsModule = module {

    viewModel { SettingsScreenViewModel() }

    viewModel {
        MicrophoneSettingsScreenViewModel()
    }
    viewModel {
        PrivacySettingsScreenViewModel()
    }
    viewModel {
        HelpScreenViewModel()
    }
    viewModel {
        AboutScreenViewModel()
    }
}
