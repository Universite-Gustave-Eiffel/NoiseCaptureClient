package org.noiseplanet.noisecapture.ui.features.permission

import Platform
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.request_permission_title
import org.jetbrains.compose.resources.StringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.features.permission.stateview.PermissionStateViewModel

class RequestPermissionScreenViewModel : ViewModel(), KoinComponent, ScreenViewModel {

    // - Properties

    private val platform: Platform by inject()
    private val permissionService: PermissionService by inject()

    val permissionStateViewModels: List<PermissionStateViewModel> = platform.requiredPermissions
        .map { permission ->
            // TODO: Avoid using ViewModel pattern at small components level, use StateHolder instead
            PermissionStateViewModel(permission)
        }

    /**
     * Emits true if all required permissions have been granted by the user
     */
    val allPermissionsGranted: Flow<Boolean> = combine(
        platform.requiredPermissions.map { permission ->
            permissionService.getPermissionStateFlow(permission)
        },
        transform = {
            it.all { permission ->
                permission == PermissionState.GRANTED
            }
        }
    )


    // - ScreenViewModel

    override val title: StringResource
        get() = Res.string.request_permission_title
}
