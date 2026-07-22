package org.noiseplanet.noisecapture.ui.features.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.app_name
import noisecapture.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarButtonViewModel
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel

class HomeScreenViewModel(
    private val onClickSettingsButton: () -> Unit,
) : ViewModel(), KoinComponent, ScreenViewModel {

    // - ScreenViewModel

    override val title: StringResource
        get() = Res.string.app_name

    override val actions: StateFlow<List<AppBarButtonViewModel>>
        get() = MutableStateFlow(
            listOf(
                AppBarButtonViewModel(
                    icon = Res.drawable.settings,
                    onClick = onClickSettingsButton,
                )
            )
        )
}
