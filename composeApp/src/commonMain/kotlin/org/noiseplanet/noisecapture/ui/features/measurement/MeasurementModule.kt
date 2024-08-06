package org.noiseplanet.noisecapture.ui.features.measurement

import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.features.measurement.indicators.AcousticIndicatorsViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.spectrum.SpectrumPlotViewModel


val measurementModule = module {

    viewModel {
        AcousticIndicatorsViewModel(measurementService = get())
    }

    viewModel {
        SpectrumPlotViewModel(measurementsService = get())
    }
}
