package org.noiseplanet.noisecapture.ui.features.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.request_permission_button_grant
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.components.button.ButtonViewModel

class RequestPermissionScreenViewModel(
    val permission: Permission,
    val image: DrawableResource,
    val title: StringResource,
    val description: StringResource,
) : ViewModel(), KoinComponent, ScreenViewModel {

    // - Properties

    private val permissionService: PermissionService by inject()

    val permissionStateFlow: Flow<PermissionState>
        get() = permissionService.getPermissionStateFlow(permission)

    val grantPermissionButtonViewModel = ButtonViewModel(
        title = Res.string.request_permission_button_grant
    )

    // - Public functions

    fun requestPermission() {
        viewModelScope.launch(context = Dispatchers.Default) {
            // We want this to run in a background thread in order not to block UI updates
            permissionService.requestPermission(permission)
        }
    }
}
