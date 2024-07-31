package org.noiseplanet.noisecapture.ui.features.permission.stateview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.request_permission_button_request
import noisecapture.composeapp.generated.resources.request_permission_button_settings
import org.jetbrains.compose.resources.stringResource

/**
 * Displays the current state of a system permission as well as controls to open settings or
 * trigger a native permission request popup
 */
@Composable
fun PermissionStateView(
    viewModel: PermissionStateViewModel,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = viewModel.permissionName,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            val icon by viewModel.stateIcon
                .collectAsState(Icons.Default.BrokenImage)
            val iconColor by viewModel.stateColor
                .collectAsState(Color.Unspecified)

            Icon(
                imageVector = icon,
                tint = iconColor,
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Button(
                onClick = { viewModel.openSettings() },
            ) {
                Text(
                    text = stringResource(Res.string.request_permission_button_settings),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }

        val shouldShowRequestButton by viewModel.shouldShowRequestButton
            .collectAsState(false)
        // If permission state is not yet determined, show a button to trigger
        // the permission request popup
        AnimatedVisibility(shouldShowRequestButton) {
            Button(
                onClick = { viewModel.requestPermission() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.request_permission_button_request),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}
