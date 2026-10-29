package org.noiseplanet.noisecapture.services.campaign

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Campaign
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import org.noiseplanet.noisecapture.services.settings.UserSettingsService
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Default campaign service storing campaign in
 */
class DefaultCampaignService : CampaignService, KoinComponent {

    // - Properties

    private val userSettingsService: UserSettingsService by inject()


    // - CampaignService

    override fun getCurrentCampaign(): Campaign? {
        val storedCampaign = userSettingsService.get(SettingsKey.CurrentCampaign) ?: return null

        if (isCampaignExpired(storedCampaign)) {
            // Stored campaign has expired, clear and return null
            userSettingsService.set(SettingsKey.CurrentCampaign, null)
            return null
        }
        return storedCampaign
    }

    override fun enroll(campaign: Campaign) {
        if (isCampaignExpired(campaign)) {
            throw CampaignService.CampaignOverException(campaign)
        }
        userSettingsService.set(SettingsKey.CurrentCampaign, campaign)
    }


    // - Private functions

    private fun isCampaignExpired(campaign: Campaign): Boolean {
        val endTimestamp = campaign.endTimestamp ?: return false
        return Instant.fromEpochMilliseconds(endTimestamp) > Clock.System.now()
    }
}
