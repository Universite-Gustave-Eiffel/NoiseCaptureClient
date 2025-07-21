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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.components.button.NCButton


@Composable
fun RequestPermissionModal(
    viewModel: RequestPermissionModalViewModel,
    onSkipButtonPress: (Permission) -> Unit,
    onGoBackButtonPress: () -> Unit,
) {
    // - Properties

    val viewState: RequestPermissionModalViewModel.ViewSate? by viewModel.viewStateFlow
        .collectAsStateWithLifecycle(null)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { _ ->
            // Only allow to close modal by pressing skip or go back button.
            false
        }
    )

    val logger: Logger = koinInject()


    // - Layout

    logger.warning("STATE: $viewState")
    val state = viewState ?: return

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
//            onSkipButtonPress()
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
                painterResource(state.illustration),
                contentDescription = "Permission illustration",
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(state.title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(state.description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            state.requestPermissionButtonViewModel?.let {
                NCButton(
                    viewModel = it,
                    modifier = Modifier.fillMaxWidth(fraction = 0.5f).height(42.dp),
                    onClick = {
                        viewModel.requestPermission(state.prompt.permission)
                    },
                )
            }
            state.openSettingsButtonViewModel?.let {
                NCButton(viewModel = it, onClick = {
                    viewModel.openSettings(state.prompt.permission)
                })
            }

            if (!state.prompt.isRequired) {
                NCButton(
                    viewModel = viewModel.skipButtonViewModel,
                    onClick = {
                        onSkipButtonPress(state.prompt.permission)
                    }
                )
            }
        }
    }
}
