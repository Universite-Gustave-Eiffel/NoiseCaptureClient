package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import org.noiseplanet.noisecapture.model.enums.CalibrationFrequencyBand
import org.noiseplanet.noisecapture.ui.navigation.CalibrationRoute


class CalibrationRouter(
    navController: NavHostController,
    backStackEntry: NavBackStackEntry,
) : Router(navController, backStackEntry) {

    // - Public functions

    fun onClickStartCalibration(durationSeconds: Int, frequencyBand: CalibrationFrequencyBand) {
        navController.navigate(CalibrationRoute(durationSeconds, frequencyBand))
    }
}
