package org.noiseplanet.noisecapture.ui.features.campaign

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.campaign_not_found_body
import noisecapture.composeapp.generated.resources.campaign_not_found_title
import noisecapture.composeapp.generated.resources.close
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.dialog.NCDialog
import org.noiseplanet.noisecapture.ui.components.dialog.NCDialogViewModel
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise

@Composable
fun CampaignNotFoundDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NCDialog(
        viewModel = NCDialogViewModel(
            title = Res.string.campaign_not_found_title,
            text = Res.string.campaign_not_found_body,
            dismissButtonContent = ButtonContent(title = Res.string.close),
            dismissButtonColors = Color.Noise.eight.transparentContainerColors(),
            containerColors = Color.Noise.eight.secondaryContainerColors(hasDropShadow = true),
        ),
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    )
}
