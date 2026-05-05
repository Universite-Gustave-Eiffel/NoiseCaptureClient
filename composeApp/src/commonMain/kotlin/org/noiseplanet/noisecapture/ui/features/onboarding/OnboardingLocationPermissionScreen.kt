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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.onboarding_continue
import noisecapture.composeapp.generated.resources.onboarding_location_permission_body
import noisecapture.composeapp.generated.resources.onboarding_location_permission_title
import noisecapture.composeapp.generated.resources.permission_location_illustration
import noisecapture.composeapp.generated.resources.request_permission_button_request
import noisecapture.composeapp.generated.resources.request_permission_button_skip
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.permission.PermissionState
import org.noiseplanet.noisecapture.ui.navigation.router.OnboardingRouter
import org.noiseplanet.noisecapture.ui.theme.defaultMarkdownTypography


@Composable
fun OnboardingLocationPermissionScreen(
    viewModel: OnboardingScreenViewModel,
    router: OnboardingRouter,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val initialPermissionState = remember { viewModel.locationPermissionState.value }
    val currentPermissionState by viewModel.locationPermissionState.collectAsStateWithLifecycle()

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
                viewModel.requestPermission(Permission.LOCATION)
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
            painter = painterResource(Res.drawable.permission_location_illustration),
            contentScale = ContentScale.Inside,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = stringResource(Res.string.onboarding_location_permission_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Markdown(
            content = stringResource(Res.string.onboarding_location_permission_body).trimIndent(),
            typography = defaultMarkdownTypography(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
