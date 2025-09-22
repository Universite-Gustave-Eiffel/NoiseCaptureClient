package org.noiseplanet.noisecapture.ui.features.debug

import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel


class DebugScreenViewModel : ViewModel(), ScreenViewModel {

    override val title: StringResource
        get() = Res.string.app_name
}
