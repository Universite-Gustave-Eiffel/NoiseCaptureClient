package org.noiseplanet.noisecapture.ui.features.details

import androidx.window.core.layout.WindowSizeClass
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.audioplayer.AudioPlayerViewModel
import org.noiseplanet.noisecapture.ui.components.map.MapViewModel
import org.noiseplanet.noisecapture.ui.components.map.MapViewModelParameters
import org.noiseplanet.noisecapture.ui.features.details.manage.ManageMeasurementViewModel

val detailsModule = module {

    viewModel { (measurement: Measurement) ->
        AudioPlayerViewModel(measurement)
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
                showControls = false,
                tilesPreloadingPadding = 0, // Since map isn't scrollable, only load necessary tiles.
            )
        )
    }
}
