package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Answer to a single question.
 */
@Serializable
sealed class QuestionAnswer {

    /**
     * Question identifier.
     */
    abstract val questionId: String

    /**
     * Slider question.
     *
     * @param value Slider value, as a 0 to 1 value.
     */
    @Serializable
    @SerialName("slider")
    data class Slider(
        override val questionId: String,
        val value: Float,
    ) : QuestionAnswer()

    /**
     * Text question answer.
     *
     * @param value Free text input from user.
     */
    @Serializable
    @SerialName("text")
    data class Text(
        override val questionId: String,
        val value: String,
    ) : QuestionAnswer()

    /**
     * Single choice question answer.
     *
     * @param value Selected choice identifier.
     */
    @Serializable
    @SerialName("single_choice")
    data class SingleChoice(
        override val questionId: String,
        val value: String
    ): QuestionAnswer()
}
