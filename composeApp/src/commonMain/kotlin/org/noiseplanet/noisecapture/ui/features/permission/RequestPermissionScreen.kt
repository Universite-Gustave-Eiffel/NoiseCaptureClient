package org.noiseplanet.noisecapture.ui.features.permission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.request_permission_button_next
import noisecapture.composeapp.generated.resources.request_permission_explanation
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.module.rememberKoinModules
import org.koin.core.annotation.KoinExperimentalAPI
import org.noiseplanet.noisecapture.ui.features.permission.stateview.PermissionStateView

/**
 * Presents required permissions to the user with controls to either request the
 * permission if it was not yet asked, or to open the corresponding settings page
 * if permission was already previously denied
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun RequestPermissionScreen(
    onClickNextButton: () -> Unit,
    viewModel: RequestPermissionScreenViewModel,
    modifier: Modifier = Modifier,
) {

    // - DI

    rememberKoinModules(unloadOnForgotten = true) {
        listOf(requestPermissionModule)
    }


    // - Layout

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

            items(viewModel.permissionStateViewModels) {
                PermissionStateView(it)
            }
            item {
                // True if all required permissions have been granted
                val allPermissionsGranted by viewModel.allPermissionsGranted
                    .collectAsState(false)

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
