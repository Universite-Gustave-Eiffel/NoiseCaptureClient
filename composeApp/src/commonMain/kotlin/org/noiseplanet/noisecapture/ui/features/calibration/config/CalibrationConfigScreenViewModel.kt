package org.noiseplanet.noisecapture.ui.features.calibration.config

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.calibration_start_button_title
import noisecapture.composeapp.generated.resources.measurement_title
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel


class CalibrationConfigScreenViewModel : ViewModel(), ScreenViewModel {

    // - Properties

    override val title: StringResource = Res.string.measurement_title

    val startButtonViewModel = NCButtonViewModel(
        title = Res.string.calibration_start_button_title,
        icon = Icons.Default.Mic,
    )
}
