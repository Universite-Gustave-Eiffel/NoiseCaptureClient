package org.noiseplanet.noisecapture.ui.features.measurement

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.features.measurement.indicators.AcousticIndicatorsViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrogram.SpectrogramPlotViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrum.SpectrumPlotViewModel


val measurementModule = module {

    viewModel {
        AcousticIndicatorsViewModel(measurementService = get())
    }

    viewModel {
        SpectrumPlotViewModel(measurementsService = get())
    }

    viewModel {
        SpectrogramPlotViewModel(
            measurementsService = get(),
            logger = get()
        )
    }

    viewModel {
        MeasurementScreenViewModel(measurementsService = get())
    }
}
