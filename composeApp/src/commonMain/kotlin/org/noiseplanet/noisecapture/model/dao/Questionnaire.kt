package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable


/**
 * Current model version, used for potential migrations.
 */
val Questionnaire.Companion.VERSION: Int get() = 1


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
