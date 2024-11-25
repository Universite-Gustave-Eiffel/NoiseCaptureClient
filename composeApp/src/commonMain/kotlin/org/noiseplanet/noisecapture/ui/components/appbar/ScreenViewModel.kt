package org.noiseplanet.noisecapture.ui.components.appbar

/**
 * Shared interface between each screen view models that provides control over the app bar
 */
interface ScreenViewModel {

    val isAppBarVisible: Boolean
        get() = true

    val actions: List<AppBarButtonViewModel>
        get() = emptyList()
}
