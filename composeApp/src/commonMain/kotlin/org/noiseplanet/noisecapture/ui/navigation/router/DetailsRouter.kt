package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import org.noiseplanet.noisecapture.ui.navigation.Route
import org.noiseplanet.noisecapture.ui.navigation.RouteIds

/**
 * Handles navigating to new screens after user takes actions on the details screen.
 */
class DetailsRouter(navController: NavHostController) : Router(navController) {

    // - Public functions

    fun onMeasurementDeleted() {
        if (navController.currentBackStackEntry?.toRoute<Route>()?.id != RouteIds.DETAILS) {
            // Measurement deleted callback might trigger twice in some cases, so before popping
            // backstack, assert that we are still on the details screen.
            return
        }
        navController.popBackStack()
    }
}
