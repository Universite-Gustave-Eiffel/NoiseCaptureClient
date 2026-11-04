package org.noiseplanet.noisecapture.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.app_name
import noisecapture.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.dao.Campaign
import org.noiseplanet.noisecapture.services.campaign.CampaignService
import org.noiseplanet.noisecapture.ui.components.appbar.AppBarButtonViewModel
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel

class HomeScreenViewModel(
    private val onClickSettingsButton: () -> Unit,
) : ViewModel(), KoinComponent, ScreenViewModel {

    // - ScreenViewModel

    override val title: StringResource
        get() = Res.string.app_name

    override val actions: StateFlow<List<AppBarButtonViewModel>>
        get() = MutableStateFlow(
            listOf(
                AppBarButtonViewModel(
                    icon = Res.drawable.settings,
                    onClick = onClickSettingsButton,
                )
            )
        )


    // - Properties

    private val campaignService: CampaignService by inject()

    val currentCampaign: StateFlow<Campaign?>
        field = MutableStateFlow(null)

    val pendingCampaign: StateFlow<Campaign?>
        field = MutableStateFlow(null)


    // - Lifecycle

    init {
        refreshCurrentCampaign()
    }


    // - Public functions

    fun refreshCurrentCampaign() {
        viewModelScope.launch {
            currentCampaign.emit(campaignService.getCurrentCampaign())
        }
    }

    fun setPendingCampaign(campaign: Campaign) {
        pendingCampaign.tryEmit(campaign)
    }

    fun dismissPendingCampaign() {
        pendingCampaign.tryEmit(null)
    }

    fun joinPendingCampaign() {
        pendingCampaign.value?.let {
            campaignService.enroll(it)
            refreshCurrentCampaign()
        }
        pendingCampaign.tryEmit(null)
    }

    fun leaveCurrentCampaign() {
        campaignService.leaveCurrentCampaign()
        pendingCampaign.tryEmit(null)
        refreshCurrentCampaign()
    }
}
