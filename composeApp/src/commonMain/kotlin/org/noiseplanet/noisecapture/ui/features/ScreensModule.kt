package org.noiseplanet.noisecapture.ui.features

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.features.details.MeasurementDetailsScreenViewModel
import org.noiseplanet.noisecapture.ui.features.history.HistoryScreenViewModel
import org.noiseplanet.noisecapture.ui.features.home.HomeScreenViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.MeasurementScreenViewModel
import org.noiseplanet.noisecapture.ui.features.permission.RequestPermissionScreenViewModel
import org.noiseplanet.noisecapture.ui.features.settings.SettingsScreenViewModel

val screensModule = module {

    viewModel { (onClickSettingsButton: () -> Unit) ->
        HomeScreenViewModel(
            onClickSettingsButton = onClickSettingsButton,
        )
    }

    viewModel {
        MeasurementScreenViewModel()
    }

    viewModel { (measurementId: String) ->
        MeasurementDetailsScreenViewModel(measurementId)
    }

    viewModel {
        HistoryScreenViewModel()
    }

    viewModel {
        RequestPermissionScreenViewModel()
    }

    viewModel {
        SettingsScreenViewModel()
    }
}
