package org.noiseplanet.noisecapture.ui.features.recording

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel
import org.noiseplanet.noisecapture.ui.features.recording.controls.RecordingControlsViewModel
import org.noiseplanet.noisecapture.ui.features.recording.plot.spectrogram.SpectrogramPlotViewModel
import org.noiseplanet.noisecapture.ui.features.recording.plot.spectrum.SpectrumPlotViewModel


val measurementRecordingModule = module {

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
