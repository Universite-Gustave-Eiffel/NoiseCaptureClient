package org.noiseplanet.noisecapture.ui.features

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.model.enums.CalibrationFrequencyBand
import org.noiseplanet.noisecapture.ui.features.calibration.analysis.CalibrationScreenViewModel
import org.noiseplanet.noisecapture.ui.features.calibration.config.CalibrationConfigScreenViewModel
import org.noiseplanet.noisecapture.ui.features.debug.DebugScreenViewModel
import org.noiseplanet.noisecapture.ui.features.details.DetailsScreenViewModel
import org.noiseplanet.noisecapture.ui.features.history.HistoryScreenViewModel
import org.noiseplanet.noisecapture.ui.features.home.HomeScreenViewModel
import org.noiseplanet.noisecapture.ui.features.map.CommunityMapScreenViewModel
import org.noiseplanet.noisecapture.ui.features.recording.RecordingScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreenViewModel
import kotlin.time.Duration


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
        CalibrationConfigScreenViewModel()
    }

    viewModel { (duration: Duration, frequencyBand: CalibrationFrequencyBand) ->
        CalibrationScreenViewModel(duration, frequencyBand)
    }

    viewModel {
        DebugScreenViewModel()
    }
}
