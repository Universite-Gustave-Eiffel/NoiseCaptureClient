package org.noiseplanet.noisecapture.ui.features.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel
import org.noiseplanet.noisecapture.util.paddingBottomWithInsets


@Composable
fun OnboardingScreenContainer(
    primaryButtonTitle: StringResource,
    primaryButtonAction: () -> Unit,
    secondaryButtonTitle: StringResource? = null,
    secondaryButtonAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 24.dp)
            .paddingBottomWithInsets(16.dp, withoutNavBar = 48.dp),
    ) {
        content()

        Spacer(modifier = Modifier.height(32.dp))

        NCButton(
            viewModel = NCButtonViewModel(title = primaryButtonTitle, hasDropShadow = true),
            onClick = primaryButtonAction,
            modifier = Modifier.height(40.dp).width(200.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (secondaryButtonTitle != null && secondaryButtonAction != null) {
            NCButton(
                viewModel = NCButtonViewModel(
                    title = secondaryButtonTitle,
                    style = NCButtonStyle.TEXT,
                    colors = { NCButtonColors.Defaults.text() }
                ),
                onClick = secondaryButtonAction,
                modifier = Modifier.height(40.dp).width(200.dp),
            )
        }
    }
}
