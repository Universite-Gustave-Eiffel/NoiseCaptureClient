package org.noiseplanet.noisecapture.ui.features.measurement

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.components.spl.SPLIndicatorsViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrogram.SpectrogramPlotViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrum.SpectrumPlotViewModel


val measurementModule = module {

    viewModel {
        SPLIndicatorsViewModel()
    }

    viewModel {
        SpectrumPlotViewModel()
    }

    viewModel {
        SpectrogramPlotViewModel()
    }

    viewModel {
        MeasurementScreenViewModel()
    }
}
