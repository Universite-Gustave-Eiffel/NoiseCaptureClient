package org.noiseplanet.noisecapture.ui.features.measurement

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.controls.RecordingControlsViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrogram.SpectrogramPlotViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.plot.spectrum.SpectrumPlotViewModel


val measurementModule = module {

    viewModel {
        SoundLevelMeterViewModel(
            showMinMaxSPL = true,
            showPlayPauseButton = false,
        )
    }

    viewModel {
        SpectrumPlotViewModel()
    }

    viewModel {
        SpectrogramPlotViewModel()
    }

    viewModel {
        RecordingControlsViewModel()
    }
}
