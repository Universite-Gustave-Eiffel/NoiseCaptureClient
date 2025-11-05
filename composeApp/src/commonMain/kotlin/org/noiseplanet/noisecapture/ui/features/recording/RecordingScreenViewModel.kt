package org.noiseplanet.noisecapture.ui.features.recording

import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel

class RecordingScreenViewModel : ViewModel(), ScreenViewModel, KoinComponent {

    // - ScreenViewModel

    override val title: StringResource
        get() = Res.string.measurement_title
}
