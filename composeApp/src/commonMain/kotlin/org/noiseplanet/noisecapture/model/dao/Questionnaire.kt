package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable

/**
 * A questionnaire with multiple pages.
 *
 * @param id Unique identifier
 * @param skippable If true, whole questionnaire can be skipped
 * @param pages Questionnaire pages
 */
@Serializable
data class Questionnaire(
    val id: String,
    val skippable: Boolean,
    val pages: List<QuestionnairePage>,
)
