package org.noiseplanet.noisecapture.ui.features.map

import androidx.lifecycle.ViewModel
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.community_map_title
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel

class CommunityMapScreenViewModel : ViewModel(), ScreenViewModel {

    // - ScreenViewModel

    override val title: StringResource
        get() = Res.string.community_map_title
    
}
