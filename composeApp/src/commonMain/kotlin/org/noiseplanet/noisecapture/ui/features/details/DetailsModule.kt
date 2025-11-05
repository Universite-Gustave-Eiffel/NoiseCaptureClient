package org.noiseplanet.noisecapture.ui.features.details

import androidx.window.core.layout.WindowSizeClass
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.components.audioplayer.AudioPlayerViewModel
import org.noiseplanet.noisecapture.ui.components.map.MapViewModel
import org.noiseplanet.noisecapture.ui.components.map.MapViewModelParameters

val detailsModule = module {

    viewModel { (measurementId: String) ->
        DetailsViewModel(measurementId)
    }

    viewModel { (filePath: String) ->
        AudioPlayerViewModel(filePath)
    }

    viewModel { (measurementId: String) ->
        ManageMeasurementViewModel(measurementId)
    }

    viewModel { (measurementId: String) ->
        SplTimePlotViewModel(measurementId)
    }

    viewModel { (windowsSizeClass: WindowSizeClass, measurementId: String) ->
        MapViewModel(
            windowsSizeClass,
            MapViewModelParameters(
                focusedMeasurementUuid = measurementId,
                // TODO: Use a shared constant for sheet peek height screen ratio.
                visibleAreaPaddingRatio = MapViewModel.VisibleAreaPaddingRatio(bottom = 0.4f),
            )
        )
    }
}
