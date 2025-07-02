package org.noiseplanet.noisecapture.ui.components.appbar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource

/**
 * Shared interface between each screen view models that provides control over the app bar
 */
interface ScreenViewModel {

    val title: StringResource

    val isAppBarVisible: Boolean
        get() = true

    val actions: StateFlow<List<AppBarButtonViewModel>>
        get() = MutableStateFlow(emptyList())
}
