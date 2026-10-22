package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable

/**
 * Answers to a questionnaire.
 *
 * @param questionnaireId Questionnaire identifier.
 * @param answers Individual questions answers.
 */
@Serializable
data class QuestionnaireAnswers(
    val questionnaireId: String,
    val answers: List<QuestionAnswer>,
)
