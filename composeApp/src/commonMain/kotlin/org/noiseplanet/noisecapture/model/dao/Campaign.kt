package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable

/**
 * A campaign of measurement in which users can participate.
 *
 * @param id Campaign unique identifier.
 * @param code Code used by users to enroll in the campaign. Should stay relatively short.
 * @param title Campaign title. Should stay short.
 * @param title Campaign subtitle. Should stay short. Optional.
 * @param description Further information regarding this campaign. Optional.
 * @param endTimestamp The date and time at which this campaign ends, in milliseconds UTC. Optional.
 * @param questionnaireId Identifier of custom questionnaire to show after measurements made while
 *                        enrolled in this campaign. Optional.
 */
@Serializable
data class Campaign(
    val id: String,
    val code: String,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val startTimestamp: Long? = null,
    val endTimestamp: Long? = null,
    val questionnaireId: String? = null,
)
