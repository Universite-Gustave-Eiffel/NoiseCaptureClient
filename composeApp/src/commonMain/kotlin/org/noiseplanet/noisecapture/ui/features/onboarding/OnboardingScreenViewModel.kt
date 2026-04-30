package org.noiseplanet.noisecapture.ui.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.onboarding_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.model.enums.AcousticsKnowledgeLevel
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import org.noiseplanet.noisecapture.services.settings.UserSettingsService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


class OnboardingScreenViewModel : ViewModel(), ScreenViewModel, KoinComponent {

    // - Properties

    override val title: StringResource = Res.string.onboarding_title

    private val settingsService: UserSettingsService by inject()
    val acousticsKnowledgeLevel: StateFlow<AcousticsKnowledgeLevel> = settingsService
        .getFlow(SettingsKey.SettingUserAcousticsKnowledge)
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = AcousticsKnowledgeLevel.BEGINNER
        )


    // - Public functions

    fun setAcousticsKnowledgeLevel(level: AcousticsKnowledgeLevel) {
        settingsService.set(SettingsKey.SettingUserAcousticsKnowledge, level)
    }
}
