package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavHostController
import org.noiseplanet.noisecapture.ui.navigation.DetailsRoute


class QuestionnaireRouter(navController: NavHostController) : Router(navController) {

    // - Public functions

    fun goToMeasurementDetails(uuid: String) {
        val route = DetailsRoute(uuid)
        navController.navigate(route)
    }
}
