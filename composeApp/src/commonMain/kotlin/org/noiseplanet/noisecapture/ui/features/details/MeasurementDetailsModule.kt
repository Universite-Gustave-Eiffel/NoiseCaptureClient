package org.noiseplanet.noisecapture.ui.features.details

import androidx.window.core.layout.WindowSizeClass
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.components.audioplayer.AudioPlayerViewModel
import org.noiseplanet.noisecapture.ui.components.map.MeasurementsMapViewModel

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

    viewModel { (measurementId: String) ->
        MeasurementSplTimePlotViewModel(measurementId)
    }

    viewModel { (windowsSizeClass: WindowSizeClass) ->
        MeasurementsMapViewModel(windowsSizeClass)
    }
}
