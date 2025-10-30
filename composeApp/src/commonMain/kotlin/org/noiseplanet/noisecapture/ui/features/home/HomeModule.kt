package org.noiseplanet.noisecapture.ui.features.home

import androidx.window.core.layout.WindowSizeClass
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.components.map.MapViewModelParameters
import org.noiseplanet.noisecapture.ui.components.map.MeasurementsMapViewModel
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel

val homeModule = module {

    viewModel {
        LastMeasurementsViewModel()
    }

    viewModel {
        SoundLevelMeterViewModel(
            showMinMaxSPL = false,
            showPlayPauseButton = true,
        )
    }

    viewModel { (windowSizeClass: WindowSizeClass) ->
        MeasurementsMapViewModel(
            windowSizeClass = windowSizeClass,
            MapViewModelParameters(
                showControls = false,
                initialZoomLevel = 15, // Show a more zoomed out map to visualize measurements
                followUserLocation = false,
            )
        )
    }
}
