package org.noiseplanet.noisecapture.ui.features.permission.stateview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.permission.PermissionService

class PermissionStateViewModel(
    private val permission: Permission,
    private val permissionService: PermissionService,
) : ViewModel() {

    private val stateFlow: Flow<PermissionState> =
        permissionService.getPermissionStateFlow(permission)

    val permissionName: String = permission.name

    val stateIcon: Flow<ImageVector> = stateFlow.map { state ->
        when (state) {
            PermissionState.GRANTED -> Icons.Default.Check
            PermissionState.DENIED -> Icons.Default.Close
            else -> Icons.Default.QuestionMark
        }
    }

    val stateColor: Flow<Color> = stateFlow.map { state ->
        when (state) {
            PermissionState.GRANTED -> Color.Green
            PermissionState.DENIED -> Color.Red
            else -> Color.Gray
        }
    }

    val shouldShowRequestButton: Flow<Boolean> = stateFlow.map { state ->
        state == PermissionState.NOT_DETERMINED
    }

    fun openSettings() {
        permissionService.openSettingsForPermission(permission)
    }

    fun requestPermission() {
        viewModelScope.launch {
            // We want this to run in a background thread in order not to block UI updates
            withContext(Dispatchers.Default) {
                permissionService.requestPermission(permission)
            }
        }
    }
}
