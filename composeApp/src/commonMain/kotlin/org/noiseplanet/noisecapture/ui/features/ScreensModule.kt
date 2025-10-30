package org.noiseplanet.noisecapture.ui.features

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.features.debug.DebugScreenViewModel
import org.noiseplanet.noisecapture.ui.features.details.DetailsScreenViewModel
import org.noiseplanet.noisecapture.ui.features.history.HistoryScreenViewModel
import org.noiseplanet.noisecapture.ui.features.home.HomeScreenViewModel
import org.noiseplanet.noisecapture.ui.features.map.CommunityMapScreenViewModel
import org.noiseplanet.noisecapture.ui.features.recording.RecordingScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreenViewModel


val screensModule = module {

    viewModel { (onClickSettingsButton: () -> Unit) ->
        HomeScreenViewModel(
            onClickSettingsButton = onClickSettingsButton,
        )
    }

    viewModel {
        RecordingScreenViewModel()
    }

    viewModel { (measurementId: String) ->
        DetailsScreenViewModel(measurementId)
    }

    viewModel {
        HistoryScreenViewModel()
    }

    viewModel {
        SettingsScreenViewModel()
    }

    viewModel {
        CommunityMapScreenViewModel()
    }

    viewModel {
        DebugScreenViewModel()
    }
}
