package org.noiseplanet.noisecapture.ui.features.permission

import androidx.lifecycle.ViewModel
import getPlatform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.PermissionService
import org.noiseplanet.noisecapture.ui.features.permission.stateview.PermissionStateViewModel

class RequestPermissionScreenViewModel(
    private val permissionService: PermissionService,
) : ViewModel(), KoinComponent {

    private val requiredPermissions = getPlatform().requiredPermissions

    val permissionStateViewModels: List<PermissionStateViewModel> = requiredPermissions
        .map { permission ->
            get<PermissionStateViewModel> { parametersOf(permission) }
        }

    /**
     * Emits true if all required permissions have been granted by the user
     */
    val allPermissionsGranted: Flow<Boolean> = combine(
        requiredPermissions.map { permission ->
            permissionService.getPermissionStateFlow(permission)
        },
        transform = {
            it.all { permission ->
                permission == PermissionState.GRANTED
            }
        }
    )
}
