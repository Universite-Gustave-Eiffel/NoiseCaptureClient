package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import org.noiseplanet.noisecapture.ui.navigation.DetailsRoute

/**
 * Handles navigating to new screens after user takes actions on the recording screen.
 */
class RecordingRouter(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
) : Router(navController, backStackEntry) {

    // - Public functions

    fun openMeasurementDetails(measurementUuid: String) {
        navController.navigate(
            DetailsRoute(
                measurementId = measurementUuid,
                parentRouteId = backStackEntry.id
            )
        )
    }
}
