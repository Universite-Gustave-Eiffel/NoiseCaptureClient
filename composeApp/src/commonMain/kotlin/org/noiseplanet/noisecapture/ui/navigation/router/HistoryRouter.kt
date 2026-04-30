package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavHostController
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.navigation.DetailsRoute

/**
 * Handles navigating to new screens after user takes actions on the history screen.
 */
class HistoryRouter(navController: NavHostController) : Router(navController) {

    // - Public functions

    fun onClickMeasurement(measurement: Measurement) {
        navController.navigate(
            route = DetailsRoute(measurement.uuid)
        )
    }
}
