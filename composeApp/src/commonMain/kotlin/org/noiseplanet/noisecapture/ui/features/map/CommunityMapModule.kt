package org.noiseplanet.noisecapture.ui.features.map

import androidx.window.core.layout.WindowSizeClass
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.components.map.MeasurementsMapViewModel

val communityMapModule = module {

    viewModel { (windowSizeClass: WindowSizeClass) ->
        MeasurementsMapViewModel(windowSizeClass)
    }
}
