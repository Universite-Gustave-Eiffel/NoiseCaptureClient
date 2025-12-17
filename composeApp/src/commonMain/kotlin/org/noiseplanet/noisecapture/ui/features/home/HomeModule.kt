package org.noiseplanet.noisecapture.ui.features.home

import androidx.window.core.layout.WindowSizeClass
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.components.map.MapViewModel
import org.noiseplanet.noisecapture.ui.components.map.MapViewModelParameters
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel

val homeModule = module {

    viewModel {
        LastMeasurementsViewModel()
    }

    viewModel { (measurementId: String) ->
        HomeRecentMeasurementViewModel(measurementId)
    }

    viewModel {
        SoundLevelMeterViewModel(
            showMinMaxSPL = false,
            showPlayPauseButton = true,
        )
    }

    viewModel { (windowSizeClass: WindowSizeClass) ->
        MapViewModel(
            windowSizeClass = windowSizeClass,
            MapViewModelParameters(
                showControls = false,
                initialZoomLevel = 15, // Show a more zoomed out map to visualize measurements
                followUserLocation = false,
                tilesPreloadingPadding = 0, // Since map isn't scrollable, only load necessary tiles.
            )
        )
    }
}
