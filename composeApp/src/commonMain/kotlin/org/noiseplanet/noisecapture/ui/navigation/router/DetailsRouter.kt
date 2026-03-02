package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController

/**
 * Handles navigating to new screens after user takes actions on the details screen.
 */
class DetailsRouter(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
) : Router(navController, backStackEntry) {

    // - Public functions

    fun onMeasurementDeleted() {
        navController.popBackStack()
    }
}
