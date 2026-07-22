package org.noiseplanet.noisecapture.ui.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.map_template
import noisecapture.composeapp.generated.resources.onboarding_continue
import noisecapture.composeapp.generated.resources.onboarding_welcome_body
import noisecapture.composeapp.generated.resources.onboarding_welcome_title
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.navigation.router.OnboardingRouter
import org.noiseplanet.noisecapture.ui.theme.Noise
import org.noiseplanet.noisecapture.ui.theme.defaultMarkdownTypography
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
                .ncDropShadow(shape = MaterialTheme.shapes.large, color = Color.Noise.one.dark)
                .clip(shape = MaterialTheme.shapes.large)
                .border(
                    width = 1.dp,
                    color = Color.Noise.one.mediumLight,
                    shape = MaterialTheme.shapes.large
                )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(Res.string.onboarding_welcome_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Markdown(
            content = stringResource(Res.string.onboarding_welcome_body).trimIndent(),
            typography = defaultMarkdownTypography(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
