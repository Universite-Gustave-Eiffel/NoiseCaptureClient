package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable

/**
 * A questionnaire page, with multiple questions.
 *
 * @param id Unique identifier.
 * @param index Page index in the questionnaire.
 * @param title Page title.
 * @param description Should give more information relative to filling up this particular page.
 * @param questions Questions in this page.
 */
@Serializable
data class QuestionnairePage(
    val id: String,
    val index: Int,
    val title: String,
    val description: String,
    val questions: List<Question>,
)
