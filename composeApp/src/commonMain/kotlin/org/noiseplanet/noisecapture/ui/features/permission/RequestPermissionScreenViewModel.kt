package org.noiseplanet.noisecapture.ui.features.permission

import Platform
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.ui.components.appbar.ScreenViewModel
import org.noiseplanet.noisecapture.ui.features.permission.stateview.PermissionStateViewModel

class RequestPermissionScreenViewModel(
    private val permissionService: PermissionService,
) : ViewModel(), KoinComponent, ScreenViewModel {

    private val platform: Platform by inject()

    val permissionStateViewModels: List<PermissionStateViewModel> = platform.requiredPermissions
        .map { permission ->
            get<PermissionStateViewModel> { parametersOf(permission) }
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
}
