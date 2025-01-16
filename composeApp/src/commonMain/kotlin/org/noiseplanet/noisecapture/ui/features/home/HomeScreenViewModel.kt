package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Settings
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_slm_button_title
import noisecapture.composeapp.generated.resources.home_slm_hint
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.services.liveaudio.LiveAudioService
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarButtonViewModel
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.components.button.ButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.ButtonViewModel
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel

class HomeScreenViewModel(
    private val onClickSettingsButton: () -> Unit,
) : ViewModel(), KoinComponent, ScreenViewModel {

    // - Properties

    private val liveAudioService: LiveAudioService by inject()

    val soundLevelMeterViewModel = SoundLevelMeterViewModel(
        showMinMaxSPL = false,
        showPlayPauseButton = true
    )

    val hintText = Res.string.home_slm_hint
    val soundLevelMeterButtonViewModel = ButtonViewModel(
        title = Res.string.home_slm_button_title,
        icon = Icons.Filled.Mic,
        style = ButtonStyle.SECONDARY,
        hasDropShadow = true
    )


    // - ScreenViewModel

    override val actions: Flow<List<AppBarButtonViewModel>>
        get() = flow {
            emit(
                listOf(
                    AppBarButtonViewModel(
                        icon = Icons.Outlined.Settings,
                        onClick = onClickSettingsButton,
                    )
                )
            )
        }
}
