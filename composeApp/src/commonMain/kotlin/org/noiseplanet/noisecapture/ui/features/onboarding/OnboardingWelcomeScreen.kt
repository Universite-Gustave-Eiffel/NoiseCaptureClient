package org.noiseplanet.noisecapture.ui.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.map_template
import noisecapture.composeapp.generated.resources.onboarding_continue
import noisecapture.composeapp.generated.resources.onboarding_welcome_body
import noisecapture.composeapp.generated.resources.onboarding_welcome_title
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.navigation.router.OnboardingRouter
import org.noiseplanet.noisecapture.util.ncDropShadow


@Composable
fun OnboardingWelcomeScreen(
    router: OnboardingRouter,
    modifier: Modifier = Modifier,
) {

    // - Layout

    OnboardingScreenContainer(
        primaryButtonTitle = Res.string.onboarding_continue,
        primaryButtonAction = { router.goToNextStep() },
        modifier = modifier,
    ) {
        Image(
            bitmap = imageResource(Res.drawable.map_template),
            contentScale = ContentScale.FillHeight,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
                .weight(1f)
                .ncDropShadow(shape = MaterialTheme.shapes.large)
                .clip(shape = MaterialTheme.shapes.large),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(Res.string.onboarding_welcome_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.onboarding_welcome_body).trimIndent(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
