package org.noiseplanet.noisecapture.ui.features

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.features.debug.DebugScreenViewModel
import org.noiseplanet.noisecapture.ui.features.details.MeasurementDetailsScreenViewModel
import org.noiseplanet.noisecapture.ui.features.history.MeasurementHistoryScreenViewModel
import org.noiseplanet.noisecapture.ui.features.home.HomeScreenViewModel
import org.noiseplanet.noisecapture.ui.features.map.CommunityMapScreenViewModel
import org.noiseplanet.noisecapture.ui.features.recording.MeasurementRecordingScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreenViewModel


val screensModule = module {

    viewModel { (onClickSettingsButton: () -> Unit) ->
        HomeScreenViewModel(
            onClickSettingsButton = onClickSettingsButton,
        )
    }

    viewModel {
        MeasurementRecordingScreenViewModel()
    }

    viewModel { (measurementId: String) ->
        MeasurementDetailsScreenViewModel(measurementId)
    }

    viewModel {
        MeasurementHistoryScreenViewModel()
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
