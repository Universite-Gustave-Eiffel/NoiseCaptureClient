package org.noiseplanet.noisecapture.ui.features.details

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.components.audioplayer.AudioPlayerViewModel

val measurementDetailsModule = module {

    viewModel { (measurementId: String) ->
        MeasurementDetailsChartsViewModel(measurementId)
    }

    viewModel { (filePath: String) ->
        AudioPlayerViewModel(filePath)
    }

    viewModel { (measurementId: String) ->
        ManageMeasurementViewModel(measurementId)
    }
}
