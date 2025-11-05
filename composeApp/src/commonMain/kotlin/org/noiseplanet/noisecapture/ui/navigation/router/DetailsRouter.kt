package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import org.noiseplanet.noisecapture.ui.navigation.DetailsRoute

/**
 * Handles navigating to new screens after user takes actions on the details screen.
 */
class DetailsRouter(
    private val navController: NavHostController,
    private val backStackEntry: NavBackStackEntry,
) {

    // - Public functions

    fun onMeasurementDeleted() {
        val parentRouteId = backStackEntry.toRoute<DetailsRoute>().parentRouteId

        if (navController.previousBackStackEntry?.id == parentRouteId) {
            navController.popBackStack()
        }
    }
}
