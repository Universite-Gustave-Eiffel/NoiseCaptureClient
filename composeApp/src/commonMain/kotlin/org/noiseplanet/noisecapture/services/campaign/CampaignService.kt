package org.noiseplanet.noisecapture.services.campaign

import org.noiseplanet.noisecapture.model.dao.Campaign
import kotlin.time.Instant

/**
 * Manages measurement campaigns
 */
interface CampaignService {

    // - Public functions

    /**
     * Returns the campaign user is currently enrolled in, if any.
     *
     * @return Current measurement campaign, null if not enrolled in any campaign.
     */
    fun getCurrentCampaign(): Campaign?

    /**
     * Unenrolls from the current campaign, if any.
     * Does nothing otherwise.
     */
    fun leaveCurrentCampaign()

    /**
     * Fetches campaign information remotely from a given invite code.
     *
     * @param code Campaign invite code
     *
     * @return Matching campaign, null if not found.
     */
    suspend fun getFromInviteCode(code: String): Campaign?

    /**
     * Enroll in the given campaign.
     *
     * @param campaign Campaign to enroll in.
     *
     * @throws CampaignOverException If campaign has an end date, and it has already passed.
     */
    fun enroll(campaign: Campaign)


    // - Exceptions

    class CampaignOverException(campaign: Campaign) : Exception(
        "Campaign ${campaign.id} already ended." + (campaign.endTimestamp?.let {
            " End date: ${Instant.fromEpochMilliseconds(it)}"
        } ?: "")
    )
}
