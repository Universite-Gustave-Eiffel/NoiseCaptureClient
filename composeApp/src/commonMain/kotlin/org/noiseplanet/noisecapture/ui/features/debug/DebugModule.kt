package org.noiseplanet.noisecapture.ui.features.debug

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.components.map.MeasurementsMapViewModel

val debugModule = module {

    viewModel {
        MeasurementsMapViewModel()
    }
}
