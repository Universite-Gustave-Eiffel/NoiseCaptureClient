package org.noiseplanet.noisecapture.ui.features.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.compose_multiplatform
import noisecapture.composeapp.generated.resources.permission_location_illustration
import noisecapture.composeapp.generated.resources.permission_microphone_illustration
import noisecapture.composeapp.generated.resources.permission_notifications_illustration
import noisecapture.composeapp.generated.resources.request_permission_button_go_back
import noisecapture.composeapp.generated.resources.request_permission_button_request
import noisecapture.composeapp.generated.resources.request_permission_button_settings
import noisecapture.composeapp.generated.resources.request_permission_button_skip
import noisecapture.composeapp.generated.resources.request_permission_location_description
import noisecapture.composeapp.generated.resources.request_permission_location_title
import noisecapture.composeapp.generated.resources.request_permission_microphone_description
import noisecapture.composeapp.generated.resources.request_permission_microphone_title
import noisecapture.composeapp.generated.resources.request_permission_notifications_description
import noisecapture.composeapp.generated.resources.request_permission_notifications_title
import noisecapture.composeapp.generated.resources.request_permission_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed


@OptIn(ExperimentalCoroutinesApi::class)
class RequestPermissionModalViewModel(
    permissionPromptFlow: SharedFlow<PermissionPrompt?>,
) : ViewModel(), KoinComponent {

    // - Associated types

    interface ViewState {

        data class Ready(
            val permission: Permission,
            val isRequired: Boolean,
            val permissionState: PermissionState,
            val title: StringResource,
            val description: StringResource,
            val illustration: DrawableResource,
        ) : ViewState

        object Loading : ViewState
    }


    // - Properties

    private val permissionService: PermissionService by inject()

    val requestPermissionButtonViewModel = NCButtonViewModel(
        title = Res.string.request_permission_button_request,
        hasDropShadow = true,
    )

    val openSettingsButtonViewModel = NCButtonViewModel(
        title = Res.string.request_permission_button_settings,
        hasDropShadow = true,
    )

    val skipButtonViewModel = NCButtonViewModel(
        title = Res.string.request_permission_button_skip,
        style = NCButtonStyle.TEXT,
        colors = { NCButtonColors.Defaults.text() }
    )

    val goBackButtonViewModel = NCButtonViewModel(
        title = Res.string.request_permission_button_go_back,
        style = NCButtonStyle.TEXT,
        colors = { NCButtonColors.Defaults.text() }
    )

    val viewStateFlow: StateFlow<ViewState> = permissionPromptFlow
        .flatMapLatest { prompt ->
            prompt?.let {
                permissionService.getPermissionStateFlow(prompt.permission)
                    .map { Pair(prompt, it) }
            } ?: flowOf(null)
        }
        .onEach { promptAndState ->
            _isVisibleFlow.tryEmit(promptAndState != null)
        }
        .filterNotNull()
        .map { (prompt, state) ->
            ViewState.Ready(
                permission = prompt.permission,
                isRequired = prompt.isRequired,
                permissionState = state,
                title = getTitleForPermission(prompt.permission),
                description = getDescriptionForPermission(prompt.permission),
                illustration = getIllustrationForPermission(prompt.permission),
            )
        }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = ViewState.Loading
        )

    private val _isVisibleFlow = MutableStateFlow(false)
    val isVisibleFlow: StateFlow<Boolean> = _isVisibleFlow


    // - Public functions

    fun requestPermission(permission: Permission) {
        viewModelScope.launch {
            permissionService.requestPermission(permission)
        }
    }

    fun openSettings(permission: Permission) {
        permissionService.openSettingsForPermission(permission)
    }


    // - Private functions

    private fun getTitleForPermission(permission: Permission) = when (permission) {
        Permission.RECORD_AUDIO -> Res.string.request_permission_microphone_title
        Permission.POST_NOTIFICATIONS -> Res.string.request_permission_notifications_title
        Permission.LOCATION_FOREGROUND, Permission.LOCATION_BACKGROUND, Permission.LOCATION_SERVICE_ON -> {
            Res.string.request_permission_location_title
        }

        else -> Res.string.request_permission_title
    }

    private fun getDescriptionForPermission(permission: Permission) = when (permission) {
        Permission.RECORD_AUDIO -> Res.string.request_permission_microphone_description
        Permission.POST_NOTIFICATIONS -> Res.string.request_permission_notifications_description
        Permission.LOCATION_FOREGROUND, Permission.LOCATION_BACKGROUND, Permission.LOCATION_SERVICE_ON -> {
            Res.string.request_permission_location_description
        }

        else -> Res.string.request_permission_title
    }

    private fun getIllustrationForPermission(permission: Permission) = when (permission) {
        Permission.RECORD_AUDIO -> Res.drawable.permission_microphone_illustration
        Permission.POST_NOTIFICATIONS -> Res.drawable.permission_notifications_illustration
        Permission.LOCATION_FOREGROUND, Permission.LOCATION_BACKGROUND, Permission.LOCATION_SERVICE_ON -> {
            Res.drawable.permission_location_illustration
        }

        else -> Res.drawable.compose_multiplatform
    }
}
