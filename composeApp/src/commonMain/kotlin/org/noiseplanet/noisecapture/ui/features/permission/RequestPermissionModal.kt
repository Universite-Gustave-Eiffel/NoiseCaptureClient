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
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.ui.components.button.NCButton


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RequestPermissionModal(
    viewModel: RequestPermissionModalViewModel,
    onSkipButtonPress: (Permission) -> Unit,
    onGoBackButtonPress: () -> Unit,
) {
    // - Properties

    val optionalState: RequestPermissionModalViewModel.ViewSate? by viewModel.viewStateFlow
        .collectAsStateWithLifecycle(null)

    val viewState = optionalState ?: return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { _ ->
            // Only allow to close modal by pressing skip or go back button.
            false
        }
    )

    val actionButtonModifier = Modifier.fillMaxWidth(fraction = 0.5f).height(42.dp)


    // - Layout

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {
            // Not dismissable
        },
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false), // Not dismissable
        contentWindowInsets = {
            WindowInsets.safeContent.only(WindowInsetsSides.Bottom)
        },
    ) {

        // Handle back press or back gesture.
        BackHandler {
            if (viewState.isRequired) {
                onGoBackButtonPress()
            } else {
                onSkipButtonPress(viewState.permission)
            }
        }

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
                painterResource(viewState.illustration),
                contentDescription = "Permission illustration",
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(viewState.title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(viewState.description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (viewState.permissionState == PermissionState.NOT_DETERMINED) {
                NCButton(
                    viewModel = viewModel.requestPermissionButtonViewModel,
                    modifier = actionButtonModifier,
                    onClick = {
                        viewModel.requestPermission(viewState.permission)
                    },
                )
            } else if (viewState.permissionState == PermissionState.DENIED) {
                NCButton(
                    viewModel = viewModel.openSettingsButtonViewModel,
                    modifier = actionButtonModifier,
                    onClick = {
                        viewModel.openSettings(viewState.permission)
                    },
                )
            }

            if (viewState.isRequired) {
                NCButton(
                    viewModel = viewModel.goBackButtonViewModel,
                    modifier = actionButtonModifier,
                    onClick = {
                        onGoBackButtonPress()
                    }
                )
            } else {
                NCButton(
                    viewModel = viewModel.skipButtonViewModel,
                    modifier = actionButtonModifier,
                    onClick = {
                        onSkipButtonPress(viewState.permission)
                    }
                )
            }
        }
    }
}
