package org.noiseplanet.noisecapture.ui.navigation

import io.ktor.http.Url
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import org.noiseplanet.noisecapture.services.settings.UserSettingsService
import org.noiseplanet.noisecapture.util.injectLogger

class DeepLinkHandler : KoinComponent {

    // - Constants

    companion object {

        const val SCHEME = "noisecapture"
    }


    // - Associated types

    enum class Actions(val urlHost: String) {
        CAMPAIGN("campaign"),
    }


    // - Properties

    private val logger: Logger by injectLogger()
    private val settingsService: UserSettingsService by inject()


    // - Public functions

    /**
     * Handles the given deeplink, verifying its components to avoid unintended usage.
     *
     * @param deepLink Incoming deeplink
     */
    fun handle(deepLink: String) {
        val url = Url(deepLink)
        if (url.protocol.name != SCHEME) {
            logger.error("$url -> Invalid deeplink")
            return
        }
        when (url.host) {
            Actions.CAMPAIGN.urlHost -> {
                url.parameters["code"]?.let { code ->
                    settingsService.set(SettingsKey.PendingCampaignInviteCode, code)
                } ?: run {
                    logger.error("$url -> Missing parameter 'code'")
                }
            }

            else -> {
                logger.error("$url -> Unsupported action '${url.host}'")
                return
            }
        }
    }
}
