package org.noiseplanet.noisecapture.ui.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.onboarding_continue
import noisecapture.composeapp.generated.resources.onboarding_mic_permission_body
import noisecapture.composeapp.generated.resources.onboarding_mic_permission_title
import noisecapture.composeapp.generated.resources.permission_microphone_illustration
import noisecapture.composeapp.generated.resources.request_permission_button_request
import noisecapture.composeapp.generated.resources.request_permission_button_skip
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.ui.navigation.router.OnboardingRouter


@Composable
fun OnboardingMicPermissionScreen(
    viewModel: OnboardingScreenViewModel,
    router: OnboardingRouter,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val initialPermissionState = remember { viewModel.microphonePermissionState.value }
    val currentPermissionState by viewModel.microphonePermissionState.collectAsStateWithLifecycle()

    if (initialPermissionState != currentPermissionState && currentPermissionState == PermissionState.GRANTED) {
        // If user just granted permission, navigate to next screen automatically
        router.goToNextStep()
    }


    // - Layout

    OnboardingScreenContainer(
        primaryButtonTitle = if (currentPermissionState != PermissionState.GRANTED) {
            Res.string.request_permission_button_request
        } else {
            Res.string.onboarding_continue
        },
        primaryButtonAction = {
            if (currentPermissionState != PermissionState.GRANTED) {
                viewModel.requestPermission(Permission.RECORD_AUDIO)
            } else {
                router.goToNextStep()
            }
        },
        secondaryButtonTitle = if (currentPermissionState != PermissionState.GRANTED) {
            Res.string.request_permission_button_skip
        } else {
            null
        },
        secondaryButtonAction = { router.goToNextStep() },
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(Res.drawable.permission_microphone_illustration),
            contentScale = ContentScale.Inside,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = stringResource(Res.string.onboarding_mic_permission_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Markdown(
            content = stringResource(Res.string.onboarding_mic_permission_body).trimIndent(),
            typography = markdownTypography(
                paragraph = MaterialTheme.typography.bodyMedium,
                textLink = TextLinkStyles(
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline,
                    ).toSpanStyle()
                )
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
