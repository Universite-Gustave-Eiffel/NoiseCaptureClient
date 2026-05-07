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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.mikepenz.markdown.m3.Markdown
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.theme.defaultMarkdownTypography


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RequestPermissionModal(
    viewModel: RequestPermissionModalViewModel,
    onSkipButtonPress: (Permission) -> Unit,
    onGoBackButtonPress: () -> Unit,
) {
    // - Properties

    val scope = rememberCoroutineScope()
    val isVisible by viewModel.isVisibleFlow.collectAsStateWithLifecycle()
    val state by viewModel.viewStateFlow.collectAsStateWithLifecycle()

    val viewState = state as? RequestPermissionModalViewModel.ViewState.Ready ?: return

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            // Only allow to close modal by pressing skip or go back button.
            when (sheetValue) {
                SheetValue.Expanded -> true
                else -> false
            }
        }
    )

    var displaySheet by remember { mutableStateOf(isVisible) }

    val actionButtonModifier = Modifier.width(200.dp).height(42.dp)


    // Show or hide sheet based on visibility property
    if (isVisible) {
        displaySheet = true
        scope.launch { sheetState.show() }
    } else {
        scope.launch { sheetState.hide() }
            .invokeOnCompletion { displaySheet = false }
    }

    // - Layout

    if (!displaySheet) return

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
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = true,
            onBackCompleted = {
                if (viewState.isRequired) {
                    onGoBackButtonPress()
                } else {
                    onSkipButtonPress(viewState.permission)
                }
            }
        )

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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
            )
            Markdown(
                content = stringResource(viewState.description).trimIndent(),
                typography = defaultMarkdownTypography(),
                modifier = Modifier.fillMaxWidth(),
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
            } else if (viewState.permissionState == PermissionState.DENIED && viewState.canOpenSettings) {
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
