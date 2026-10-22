package org.noiseplanet.noisecapture.ui.navigation.router

import androidx.navigation.NavHostController
import org.noiseplanet.noisecapture.ui.navigation.QuestionnaireRoute

/**
 * Handles navigating to new screens after user takes actions on the recording screen.
 */
class RecordingRouter(navController: NavHostController) : Router(navController) {

    // - Public functions

    fun goToQuestionnaire(
        measurementId: String,
        questionnaireId: String = "soundscape_default_questionnaire", // TODO: Dynamic questionnaire
    ) {
        navController.navigate(
            QuestionnaireRoute(
                measurementId = measurementId,
                questionnaireId = questionnaireId,
                isReviewMode = false
            )
        )
    }
}
