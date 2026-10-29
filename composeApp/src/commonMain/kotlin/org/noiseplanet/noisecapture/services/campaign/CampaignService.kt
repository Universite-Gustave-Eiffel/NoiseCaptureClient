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
     * Enroll in the given campaign.
     *
     * @param campaign Campaign to enroll in.
     *
     * @throws CampaignOverException If campaign has an end date and it has already passed.
     */
    fun enroll(campaign: Campaign)


    // - Exceptions

    class CampaignOverException(campaign: Campaign) : Exception(
        "Campaign ${campaign.id} already ended." + (campaign.endTimestamp?.let {
            " End date: ${Instant.fromEpochMilliseconds(it)}"
        } ?: "")
    )
}
