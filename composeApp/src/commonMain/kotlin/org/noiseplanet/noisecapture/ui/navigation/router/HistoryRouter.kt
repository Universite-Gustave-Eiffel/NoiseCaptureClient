package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.navigation.DetailsRoute

/**
 * Handles navigating to new screens after user takes actions on the history screen.
 */
class HistoryRouter(
    private val navController: NavHostController,
    private val backStackEntry: NavBackStackEntry,
) {

    // - Public functions

    fun onClickMeasurement(measurement: Measurement) {
        navController.navigate(
            route = DetailsRoute(
                measurementId = measurement.uuid,
                parentRouteId = backStackEntry.id,
            )
        )
    }
}
