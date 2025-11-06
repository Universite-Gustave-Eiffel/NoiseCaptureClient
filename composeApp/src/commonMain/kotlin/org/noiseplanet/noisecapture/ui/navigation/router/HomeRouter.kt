package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.navigation.CommunityMapRoute
import org.noiseplanet.noisecapture.ui.navigation.DetailsRoute
import org.noiseplanet.noisecapture.ui.navigation.HistoryRoute
import org.noiseplanet.noisecapture.ui.navigation.RecordingRoute
import org.noiseplanet.noisecapture.ui.navigation.SettingsRoute


/**
 * Handles navigating to new screens after user takes actions on the home screen.
 */
class HomeRouter(
    private val navController: NavHostController,
    private val backStackEntry: NavBackStackEntry,
    val showPermissionPrompt: (Permission) -> Unit,
) {

    // - Public functions

    fun onClickMeasurement(measurement: Measurement) {
        navController.navigate(
            DetailsRoute(
                measurement.uuid,
                backStackEntry.id,
            )
        )
        navController.currentBackStackEntry
    }

    fun onClickOpenSoundLevelMeterButton() {
        navController.navigate(RecordingRoute())
    }

    fun onClickOpenHistoryButton() {
        navController.navigate(HistoryRoute())
    }

    fun onClickSettingsButton() {
        navController.navigate(SettingsRoute())
    }

    fun onClickOpenMapButton() {
        navController.navigate(CommunityMapRoute())
    }
}
