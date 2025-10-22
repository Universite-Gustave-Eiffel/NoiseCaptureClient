package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.navigation.HistoryRoute
import org.noiseplanet.noisecapture.ui.navigation.MeasurementDetailsRoute
import org.noiseplanet.noisecapture.ui.navigation.MeasurementRecordingRoute
import org.noiseplanet.noisecapture.ui.navigation.SettingsRoute


/**
 * Handles navigating to new screens after user takes actions on the home screen.
 */
class HomeRouter(
    private val navController: NavHostController,
    private val backStackEntry: NavBackStackEntry,
) {

    // - Public functions

    fun onClickMeasurement(measurement: Measurement) {
        navController.navigate(
            MeasurementDetailsRoute(
                measurement.uuid,
                backStackEntry.id,
            )
        )
        navController.currentBackStackEntry
    }

    fun onClickOpenSoundLevelMeterButton() {
        navController.navigate(MeasurementRecordingRoute())
    }

    fun onClickOpenHistoryButton() {
        navController.navigate(HistoryRoute())
    }

    fun onClickSettingsButton() {
        navController.navigate(SettingsRoute())
    }

    fun onClickOpenMapButton() {
        
    }
}
