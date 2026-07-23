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
    val pages: List<Page>,
)


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
data class Page(
    val id: String,
    val index: Int,
    val title: String,
    val description: String,
    val questions: List<Question>,
)


/**
 * A question in a questionnaire.
 *
 * @param id Unique identifier
 * @param title The question itself
 * @param description More information
 * @param startLabel Slider start (or left) label
 * @param endLabel Slider end (or right) label
 */
@Serializable
data class Question(
    val id: String,
    val title: String,
    val description: String,
    val startLabel: String,
    val endLabel: String,
)


@Serializable
data class QuestionnaireAnswers(
    val questionnaireId: String,
    val answers: List<QuestionAnswer>,
)


@Serializable
data class QuestionAnswer(
    val questionId: String,
    val answer: Double,
)
