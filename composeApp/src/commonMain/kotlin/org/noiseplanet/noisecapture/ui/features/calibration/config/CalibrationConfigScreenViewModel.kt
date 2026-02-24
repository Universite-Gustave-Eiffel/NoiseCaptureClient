package org.noiseplanet.noisecapture.ui.features.calibration.config

import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.measurement_title
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel


class CalibrationConfigScreenViewModel : ViewModel(), ScreenViewModel {

    // - Properties

    override val title: StringResource = Res.string.measurement_title
}
