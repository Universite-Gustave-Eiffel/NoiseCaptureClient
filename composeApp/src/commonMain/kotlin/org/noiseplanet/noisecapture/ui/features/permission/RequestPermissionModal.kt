package org.noiseplanet.noisecapture.ui.features.permission

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.components.button.NCButton


@Composable
fun RequestPermissionModal(
    permission: Permission,
    isRequired: Boolean,
) {
    // - Properties

    val viewModel: RequestPermissionModalViewModel = koinViewModel { parametersOf(permission) }
    val viewState: RequestPermissionModalViewModel.ViewSate by viewModel.viewStateFlow.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { _ ->
            // Only optional permission prompts can be dismissed
            !isRequired
        }
    )
    var showBottomSheet by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()


    // - Layout

    if (!showBottomSheet) return

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            showBottomSheet = false
        },
        contentWindowInsets = {
            WindowInsets.safeContent.only(WindowInsetsSides.Bottom)
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 32.dp,
                ),
        ) {
            Image(
                painterResource(viewModel.illustration),
                contentDescription = "Permission illustration",
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(viewModel.title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(viewModel.description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            viewState.requestPermissionButtonViewModel?.let {
                NCButton(
                    viewModel = it,
                    modifier = Modifier.fillMaxWidth(fraction = 0.5f).height(42.dp),
                    onClick = {
                        viewModel.requestPermission()
                    },
                )
            }
            viewState.openSettingsButtonViewModel?.let {
                NCButton(viewModel = it, onClick = {
                    viewModel.openSettings()
                })
            }

            if (!isRequired) {
                NCButton(
                    viewModel = viewModel.skipButtonViewModel,
                    onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }
                )
            }
        }
    }
}
