package org.noiseplanet.noisecapture.ui.features.measurement

import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel
import org.noiseplanet.noisecapture.ui.features.measurement.controls.RecordingControlsViewModel

class MeasurementScreenViewModel : ViewModel(), ScreenViewModel, KoinComponent {

    // - Properties

    val soundLevelMeterViewModel: SoundLevelMeterViewModel by inject()
    val recordingControlsViewModel = RecordingControlsViewModel()


    // - ScreenViewModel

    override val title: StringResource
        get() = Res.string.measurement_title
}
