package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable

/**
 * A questionnaire with multiple pages.
 *
 * @param id Unique identifier
 * @param pages Questionnaire pages
 */
@Serializable
data class Questionnaire(
    val id: String,
    val pages: List<QuestionnairePage>,
)
