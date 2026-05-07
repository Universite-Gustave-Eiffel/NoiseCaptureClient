package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavHostController

/**
 * Base router class that allow common interactions with navigation controller
 */
abstract class Router(
    protected val navController: NavHostController,
) {
    // - Public functions

    open fun popBackStack() = navController.popBackStack()
}
