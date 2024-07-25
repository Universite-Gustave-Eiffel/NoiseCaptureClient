package org.noiseplanet.noisecapture.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import getPlatform
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.request_permission_button_next
import noisecapture.composeapp.generated.resources.request_permission_button_request
import noisecapture.composeapp.generated.resources.request_permission_button_settings
import noisecapture.composeapp.generated.resources.request_permission_explanation
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.permission.PermissionService
import org.noiseplanet.noisecapture.permission.PermissionState

/**
 * Presents required permissions to the user with controls to either request the
 * permission if it was not yet asked, or to open the corresponding settings page
 * if permission was already previously denied
 *
 * TODO: Use view models to provide data to the interface
 * TODO: Rethink package structure to split views into smaller components
 */
@Composable
fun RequestPermissionScreen(
    onClickNextButton: () -> Unit,
    permissionService: PermissionService = koinInject(),
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

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

            val requiredPermissions = getPlatform().requiredPermissions
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
                        Text(
                            text = permission.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
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
                            Text(
                                text = stringResource(Res.string.request_permission_button_settings),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }

                    // If permission state is not yet determined, show a button to trigger
                    // the permission request popup
                    AnimatedVisibility(permissionState == PermissionState.NOT_DETERMINED) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    permissionService.requestPermission(permission)
                                }
                            },
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
            item {
                // True if all required permissions have been granted
                val allPermissionsGranted by combine(
                    requiredPermissions.map { permission ->
                        permissionService.getPermissionStateFlow(permission)
                    },
                    transform = {
                        it.all { permission ->
                            permission == PermissionState.GRANTED
                        }
                    }
                ).collectAsState(false)

                AnimatedVisibility(allPermissionsGranted) {
                    // Show Next button only if all required permissions have been granted
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.fillParentMaxWidth())
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Button(onClick = onClickNextButton) {
                                Text(stringResource(Res.string.request_permission_button_next))
                            }
                        }
                    }
                }
            }
        }
    }
}
