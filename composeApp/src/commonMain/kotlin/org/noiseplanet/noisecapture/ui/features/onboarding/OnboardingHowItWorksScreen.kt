package org.noiseplanet.noisecapture.ui.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.m3.Markdown
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.onboarding_continue
import noisecapture.composeapp.generated.resources.onboarding_how_it_works_body
import noisecapture.composeapp.generated.resources.onboarding_how_it_works_title
import noisecapture.composeapp.generated.resources.onboarding_illustration_listening
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.navigation.router.OnboardingRouter
import org.noiseplanet.noisecapture.ui.theme.defaultMarkdownTypography


@Composable
fun OnboardingHowItWorksScreen(
    router: OnboardingRouter,
    modifier: Modifier = Modifier,
) {

    // - Layout

    OnboardingScreenContainer(
        primaryButtonTitle = Res.string.onboarding_continue,
        primaryButtonAction = { router.goToNextStep() },
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.weight(1f),
        ) {
            Image(
                painter = painterResource(Res.drawable.onboarding_illustration_listening),
                contentScale = ContentScale.Inside,
                contentDescription = null,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(Res.string.onboarding_how_it_works_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Markdown(
            content = stringResource(Res.string.onboarding_how_it_works_body).trimIndent(),
            typography = defaultMarkdownTypography(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
