package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A question in a questionnaire.
 */
@Serializable
sealed class Question {

    // - Shared properties

    /**
     * Unique identifier
     */
    abstract val id: String

    /**
     * The question itself
     */
    abstract val title: String

    /**
     * Additional information (optional)
     */
    abstract val description: String?

    /**
     * If true, must be answered to proceed
     */
    abstract val required: Boolean


    // - Question types

    /**
     * Slider question.
     *
     * @param startLabel Slider start (or left) label.
     * @param endLabel Slider end (or right) label.
     */
    @Serializable
    @SerialName("slider")
    data class Slider(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val required: Boolean,
        val startLabel: String,
        val endLabel: String,
    ) : Question()

    /**
     * Free text question.
     *
     * @param placeholder Text field placeholder (optional)
     */
    @Serializable
    @SerialName("text")
    data class Text(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val required: Boolean,
        val placeholder: String?,
    ) : Question()

    /**
     * Multiple choice question with one selectable option
     *
     * @param choices Available answer options.
     */
    @Serializable
    @SerialName("single_choice")
    data class SingleChoice(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val required: Boolean,
        val choices: List<Choice>,
    ) : Question()
}


/**
 * An answer option for multiple choices questions
 *
 * @param id Unique identifier.
 * @param title Text that will be displayed for this option.
 */
@Serializable
data class Choice(
    val id: String,
    val title: String,
)
