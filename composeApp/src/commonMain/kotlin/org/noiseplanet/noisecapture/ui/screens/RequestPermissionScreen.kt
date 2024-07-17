package org.noiseplanet.noisecapture.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.request_permission_explanation
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionService
import org.noiseplanet.noisecapture.permission.PermissionState

/**
 * Presents required permissions to the user with controls to either request the
 * permission if it was not yet asked, or to open the corresponding settings page
 * if permission was already previously denied
 */
@Composable
fun RequestPermissionScreen(
    permissionService: PermissionService,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 64.dp,
                start = 16.dp,
                end = 16.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = stringResource(Res.string.request_permission_explanation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            val requiredPermissions = arrayOf(
                Permission.RECORD_AUDIO,
                Permission.LOCATION_SERVICE_ON,
                Permission.LOCATION_FOREGROUND,
                Permission.LOCATION_BACKGROUND,
            )
            items(requiredPermissions) { permission ->
                val permissionState by mutableStateOf(permissionService.checkPermission(permission))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row {
                        Text(
                            text = permission.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Icon(
                        imageVector = when (permissionState) {
                            PermissionState.GRANTED -> Icons.Default.Check
                            else -> Icons.Default.Close
                        },
                        tint = when (permissionState) {
                            PermissionState.GRANTED -> Color.Green
                            else -> Color.Red
                        },
                        modifier = Modifier.padding(horizontal = 8.dp),
                        contentDescription = null,
                    )
                }
            }
        }
    }
}
