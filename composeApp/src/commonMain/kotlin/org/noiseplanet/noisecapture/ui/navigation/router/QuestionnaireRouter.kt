package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavHostController
import org.noiseplanet.noisecapture.ui.navigation.DetailsRoute
import org.noiseplanet.noisecapture.ui.navigation.QuestionnaireRoute


class QuestionnaireRouter(navController: NavHostController) : Router(navController) {

    // - Public functions

    fun goToMeasurementDetails(uuid: String) {
        val route = DetailsRoute(uuid)

        navController.navigate(route) {
            // When exiting questionnaire, show the measurement details route but
            // remove questionnaire from backstack so that going back brings the user
            // directly to the recording page again.
            popUpTo<QuestionnaireRoute> { inclusive = true }
            launchSingleTop = true
        }
    }
}
