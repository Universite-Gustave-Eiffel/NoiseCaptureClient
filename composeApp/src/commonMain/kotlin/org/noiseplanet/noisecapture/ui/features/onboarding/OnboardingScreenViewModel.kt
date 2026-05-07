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
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.services.settings.SettingsKey
import org.noiseplanet.noisecapture.services.settings.UserSettingsService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


class OnboardingScreenViewModel : ViewModel(), ScreenViewModel, KoinComponent {

    // - Properties

    private val settingsService: UserSettingsService by inject()
    private val permissionService: PermissionService by inject()

    override val title: StringResource = Res.string.onboarding_title

    val acousticsKnowledgeLevel: StateFlow<AcousticsKnowledgeLevel> = settingsService
        .getFlow(SettingsKey.SettingUserAcousticsKnowledge)
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = AcousticsKnowledgeLevel.BEGINNER
        )

    val microphonePermissionState: StateFlow<PermissionState> = permissionService
        .getPermissionStateFlow(Permission.RECORD_AUDIO)
    val locationPermissionState: StateFlow<PermissionState> = permissionService
        .getPermissionStateFlow(Permission.LOCATION)


    // - Public functions

    fun setAcousticsKnowledgeLevel(level: AcousticsKnowledgeLevel) {
        settingsService.set(SettingsKey.SettingUserAcousticsKnowledge, level)
    }

    fun requestPermission(permission: Permission) {
        when (permissionService.getPermissionState(permission)) {
            PermissionState.NOT_DETERMINED -> permissionService.requestPermission(permission)
            PermissionState.DENIED -> permissionService.openSettingsForPermission(permission)
            else -> return
        }
    }
}
