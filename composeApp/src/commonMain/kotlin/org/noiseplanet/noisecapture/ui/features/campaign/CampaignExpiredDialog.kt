package org.noiseplanet.noisecapture.ui.features.campaign

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.campaign_expired_body
import noisecapture.composeapp.generated.resources.campaign_expired_title
import noisecapture.composeapp.generated.resources.close
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.model.dao.Campaign
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.dialog.NCDialog
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise

@Composable
fun CampaignExpiredDialog(
    campaign: Campaign,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NCDialog(
        title = {
            Text(text = stringResource(Res.string.campaign_expired_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(text = stringResource(Res.string.campaign_expired_body))

                Column {
                    Text(
                        text = campaign.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    campaign.subtitle?.let { subtitle ->
                        Text(text = subtitle)
                    }
                }

                CampaignDetailsView(campaign)
            }
        },
        confirmButton = {
            NCButton(
                content = ButtonContent(title = Res.string.close),
                colors = Color.Noise.ten.transparentContainerColors(),
                onClick = onDismissRequest,
            )
        },
        containerColors = Color.Noise.ten.secondaryContainerColors(hasDropShadow = true),
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    )
}
