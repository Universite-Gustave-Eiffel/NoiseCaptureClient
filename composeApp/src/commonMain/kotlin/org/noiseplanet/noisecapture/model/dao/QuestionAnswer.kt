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

    @Serializable
    @SerialName("slider")
    data class Slider(
        override val questionId: String,
        val value: Float,
    ) : QuestionAnswer()

    @Serializable
    @SerialName("text")
    data class Text(
        override val questionId: String,
        val value: String,
    ) : QuestionAnswer()
}
