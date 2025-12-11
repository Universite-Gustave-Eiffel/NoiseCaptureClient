package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

/**
 * Base router class that allow common interactions with navigation controller
 */
abstract class Router(
    protected val navController: NavHostController,
    protected val backStackEntry: NavBackStackEntry,
) {
    // - Public functions

    fun popBackStack() = navController.popBackStack()
}
