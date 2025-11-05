package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import org.noiseplanet.noisecapture.ui.navigation.DetailsRoute

/**
 * Handles navigating to new screens after user takes actions on the recording screen.
 */
class RecordingRouter(
    private val navController: NavHostController,
    private val backStackEntry: NavBackStackEntry,
) {

    // - Public functions

    fun onMeasurementDone(measurementUuid: String) {
        navController.navigate(
            DetailsRoute(
                measurementId = measurementUuid,
                parentRouteId = backStackEntry.id
            )
        )
    }
}
