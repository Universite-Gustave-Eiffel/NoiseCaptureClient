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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.services.permission.PermissionService
import org.noiseplanet.noisecapture.util.stateInWhileSubscribed

class PermissionStateViewModel(
    private val permission: Permission,
) : ViewModel(), KoinComponent {

    // - Properties

    private val permissionService: PermissionService by inject()

    private val stateFlow: Flow<PermissionState> =
        permissionService.getPermissionStateFlow(permission)

    val permissionName: String = permission.name

    val stateIcon: StateFlow<ImageVector> = stateFlow
        .map { state ->
            when (state) {
                PermissionState.GRANTED -> Icons.Default.Check
                PermissionState.DENIED -> Icons.Default.Close
                else -> Icons.Default.QuestionMark
            }
        }.stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = Icons.Default.QuestionMark
        )

    val stateColor: StateFlow<Color> = stateFlow
        .map { state ->
            when (state) {
                PermissionState.GRANTED -> Color.Green
                PermissionState.DENIED -> Color.Red
                else -> Color.Gray
            }
        }.stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = Color.Gray
        )

    val shouldShowRequestButton: StateFlow<Boolean> = stateFlow
        .map { it == PermissionState.NOT_DETERMINED }
        .stateInWhileSubscribed(
            scope = viewModelScope,
            initialValue = false,
        )


    // - Public functions

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
