package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.menu_feedback
import noisecapture.composeapp.generated.resources.menu_history
import noisecapture.composeapp.generated.resources.menu_map
import noisecapture.composeapp.generated.resources.menu_new_measurement
import noisecapture.composeapp.generated.resources.menu_settings
import noisecapture.composeapp.generated.resources.menu_statistics
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.audio.AudioSourceState
import org.noiseplanet.noisecapture.services.liveaudio.LiveAudioService
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarButtonViewModel
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterViewModel
import org.noiseplanet.noisecapture.ui.features.home.menuitem.MenuItemViewModel
import org.noiseplanet.noisecapture.ui.navigation.Route

class HomeScreenViewModel(
    private val onClickSettingsButton: () -> Unit,
) : ViewModel(), KoinComponent, ScreenViewModel {

    // - Properties

    private val liveAudioService: LiveAudioService by inject()

    val soundLevelMeterViewModel = get<SoundLevelMeterViewModel>().apply {
        showMinMaxSPL = false
        showPlayPauseButton = true
    }


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


    val menuItems: Array<MenuItemViewModel> = arrayOf(
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_new_measurement, Icons.Filled.Mic, Route.RequestPermission)
        },
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_history, Icons.Filled.History, null)
        },
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_feedback, Icons.Filled.HistoryEdu, null)
        },
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_statistics, Icons.Filled.Timeline, null)
        },
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_map, Icons.Filled.Map, null)
        },
        get<MenuItemViewModel> {
            parametersOf(Res.string.menu_settings, Icons.Filled.Settings, Route.Settings)
        },
    )


    // - Public functions

    fun setupAudioSource() = liveAudioService.setupAudioSource()

    fun releaseAudioSource() = liveAudioService.releaseAudioSource()
}
