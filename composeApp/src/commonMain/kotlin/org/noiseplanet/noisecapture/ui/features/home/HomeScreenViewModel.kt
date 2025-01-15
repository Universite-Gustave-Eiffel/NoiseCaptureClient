package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_slm_button_title
import noisecapture.composeapp.generated.resources.home_slm_hint
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.audio.AudioSourceState
import org.noiseplanet.noisecapture.services.liveaudio.LiveAudioService
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarButtonViewModel
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel

class HomeScreenViewModel(
    private val onClickSettingsButton: () -> Unit,
) : ViewModel(), KoinComponent, ScreenViewModel {

    // - Properties

    private val liveAudioService: LiveAudioService by inject()

    val soundLevelMeterViewModel = get<SoundLevelMeterViewModel>().apply {
        showMinMaxSPL = false
        showPlayPauseButton = true
    }

    val hintText = Res.string.home_slm_hint
    val openSoundLevelMeterButtonTitle = Res.string.home_slm_button_title


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


    // - Lifecycle

    init {
        viewModelScope.launch {
            liveAudioService.audioSourceStateFlow.collect { state ->
                if (state == AudioSourceState.READY) {
                    // Start listening to incoming audio whenever audio source is done initializing
                    soundLevelMeterViewModel.startListening()
                }
            }
        }
    }


    // - Public functions

    fun setupAudioSource() = liveAudioService.setupAudioSource()

    fun releaseAudioSource() = liveAudioService.releaseAudioSource()
}
