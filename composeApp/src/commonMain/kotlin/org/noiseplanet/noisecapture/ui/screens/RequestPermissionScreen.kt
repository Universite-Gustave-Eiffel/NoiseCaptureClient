package org.noiseplanet.noisecapture.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.request_permission_explanation
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.logger.Logger
import org.koin.mp.KoinPlatformTools
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
    permissionService: PermissionService = koinInject(),
    logger: Logger = KoinPlatformTools.defaultLogger(),
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
                val permissionState: PermissionState by permissionService
                    .getPermissionStateFlow(permission)
                    .collectAsState(PermissionState.NOT_DETERMINED)

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        androidx.compose.material.Text(
                            text = permission.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        androidx.compose.material.Icon(
                            imageVector = when (permissionState) {
                                PermissionState.GRANTED -> Icons.Default.Check
                                PermissionState.NOT_DETERMINED -> Icons.Default.QuestionMark
                                else -> Icons.Default.Close
                            },
                            tint = when (permissionState) {
                                PermissionState.GRANTED -> Color.Green
                                PermissionState.NOT_DETERMINED -> Color.Gray
                                else -> Color.Red
                            },
                            contentDescription = null,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Button(
                            onClick = {
                                permissionService.openSettingsForPermission(permission)
                            },
                        ) {
                            androidx.compose.material.Text(
                                text = "Settings",
                                color = androidx.compose.material.MaterialTheme.colors.onPrimary,
                            )
                        }
                    }
                    
                    // If permission state is not yet determined, show a button to trigger
                    // the permission request popup
                    AnimatedVisibility(permissionState == PermissionState.NOT_DETERMINED) {
                        Button(
                            onClick = {
                                permissionService.requestPermission(permission)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material.Text(
                                text = "Request",
                                color = androidx.compose.material.MaterialTheme.colors.onPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}
