package org.noiseplanet.noisecapture.ui.features.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.compose_multiplatform
import noisecapture.composeapp.generated.resources.permission_location_illustration
import noisecapture.composeapp.generated.resources.permission_microphone_illustration
import noisecapture.composeapp.generated.resources.permission_notifications_illustration
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


class RequestPermissionModalViewModel(
    private val permission: Permission,
) : ViewModel(), KoinComponent {

    // - Associated types

    data class ViewSate(
        val requestPermissionButtonViewModel: NCButtonViewModel? = null,
        val openSettingsButtonViewModel: NCButtonViewModel? = null,
    )


    // - Properties

    private val permissionService: PermissionService by inject()

    private val permissionStateFlow: Flow<PermissionState> = permissionService
        .getPermissionStateFlow(permission)

    private val requestPermissionButtonViewModel = NCButtonViewModel(
        title = Res.string.request_permission_button_request,
        hasDropShadow = true,
    )

    private val openSettingsButtonViewModel = NCButtonViewModel(
        title = Res.string.request_permission_button_settings,
        hasDropShadow = true,
    )

    val skipButtonViewModel = NCButtonViewModel(
        title = Res.string.request_permission_button_skip,
        style = NCButtonStyle.TEXT,
        colors = { NCButtonColors.Defaults.text() }
    )

    val illustration: DrawableResource = when (permission) {
        Permission.RECORD_AUDIO -> Res.drawable.permission_microphone_illustration
        Permission.POST_NOTIFICATIONS -> Res.drawable.permission_notifications_illustration
        Permission.LOCATION_FOREGROUND, Permission.LOCATION_BACKGROUND, Permission.LOCATION_SERVICE_ON -> {
            Res.drawable.permission_location_illustration
        }

        else -> Res.drawable.compose_multiplatform
    }
    val title: StringResource = when (permission) {
        Permission.RECORD_AUDIO -> Res.string.request_permission_microphone_title
        Permission.POST_NOTIFICATIONS -> Res.string.request_permission_notifications_title
        Permission.LOCATION_FOREGROUND, Permission.LOCATION_BACKGROUND, Permission.LOCATION_SERVICE_ON -> {
            Res.string.request_permission_location_title
        }

        else -> Res.string.request_permission_title
    }
    val description: StringResource = when (permission) {
        Permission.RECORD_AUDIO -> Res.string.request_permission_microphone_description
        Permission.POST_NOTIFICATIONS -> Res.string.request_permission_notifications_description
        Permission.LOCATION_FOREGROUND, Permission.LOCATION_BACKGROUND, Permission.LOCATION_SERVICE_ON -> {
            Res.string.request_permission_location_description
        }

        else -> Res.string.request_permission_title
    }

    val viewStateFlow: StateFlow<ViewSate> = permissionStateFlow
        .map { permissionState ->
            when (permissionState) {
                PermissionState.NOT_DETERMINED -> ViewSate(
                    requestPermissionButtonViewModel = requestPermissionButtonViewModel
                )

                PermissionState.DENIED -> ViewSate(
                    openSettingsButtonViewModel = openSettingsButtonViewModel
                )

                else -> ViewSate()
            }
        }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = ViewSate()
        )


    // - Public functions

    fun requestPermission() {
        viewModelScope.launch {
            permissionService.requestPermission(permission)
        }
    }

    fun openSettings() {
        permissionService.openSettingsForPermission(permission)
    }
}
