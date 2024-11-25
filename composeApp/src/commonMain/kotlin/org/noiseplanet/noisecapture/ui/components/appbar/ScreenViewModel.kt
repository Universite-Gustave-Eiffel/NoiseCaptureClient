package org.noiseplanet.noisecapture.ui.components.appbar

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Shared interface between each screen view models that provides control over the app bar
 */
interface ScreenViewModel {

    val isAppBarVisible: Boolean
        get() = true

    val actions: Flow<List<AppBarButtonViewModel>>
        get() = MutableStateFlow(emptyList())
}
