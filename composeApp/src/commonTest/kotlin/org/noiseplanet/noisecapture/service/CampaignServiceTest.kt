package org.noiseplanet.noisecapture.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.noiseplanet.noisecapture.TestClock
import org.noiseplanet.noisecapture.model.dao.Campaign
import org.noiseplanet.noisecapture.services.campaign.CampaignService
import org.noiseplanet.noisecapture.services.campaign.DefaultCampaignService
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds

class CampaignServiceTest {

    @Test
    fun testEnrollAndLeaveCampaign() = runTest {
        val settingsService = MockUserSettingsService()
        val campaignService = DefaultCampaignService(userSettingsService = settingsService)
        val campaign = Campaign(
            id = "test",
            code = "123456",
            title = "test",
        )

        assertNull(campaignService.getCurrentCampaign())

        campaignService.enroll(campaign)
        assertEquals(campaign, campaignService.getCurrentCampaign())
        assertEquals(campaign, settingsService.get(SettingsKey.CurrentCampaign))

        campaignService.leaveCurrentCampaign()
        assertNull(campaignService.getCurrentCampaign())
        assertNull(settingsService.get(SettingsKey.CurrentCampaign))
    }

    @Test
    fun testEnrollExpiredCampaign() = runTest {
        val clock = TestClock(testScheduler)
        val settingsService = MockUserSettingsService()
        val campaignService = DefaultCampaignService(
            userSettingsService = settingsService,
            clock = clock,
        )
        val campaign = Campaign(
            id = "test",
            code = "123456",
            title = "test",
            endTimestamp = clock.now().toEpochMilliseconds() - 1_000
        )

        assertFailsWith(CampaignService.CampaignOverException::class) {
            campaignService.enroll(campaign)
        }
    }

    @Test
    fun testLeaveExpiredCampaignAutomatically() = runTest {
        val clock = TestClock(testScheduler)
        val settingsService = MockUserSettingsService()
        val campaignService = DefaultCampaignService(
            userSettingsService = settingsService,
            clock = clock,
        )
        val campaign = Campaign(
            id = "test",
            code = "123456",
            title = "test",
            endTimestamp = clock.now().toEpochMilliseconds() + 1_000
        )

        campaignService.enroll(campaign)
        assertEquals(campaign, campaignService.getCurrentCampaign())

        delay(2.seconds)
        assertNull(campaignService.getCurrentCampaign())
    }
}
