package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavHostController
import org.noiseplanet.noisecapture.ui.navigation.QuestionnaireRoute

/**
 * Handles navigating to new screens after user takes actions on the recording screen.
 */
class RecordingRouter(navController: NavHostController) : Router(navController) {

    // - Public functions

    fun goToQuestionnaire(measurementUuid: String) {
        navController.navigate(QuestionnaireRoute(measurementUuid))
    }
}
