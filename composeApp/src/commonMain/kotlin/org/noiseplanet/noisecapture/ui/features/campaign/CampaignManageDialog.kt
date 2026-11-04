package org.noiseplanet.noisecapture.ui.features.campaign

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.campaign_join_disclaimer
import noisecapture.composeapp.generated.resources.campaign_manage_join_button
import noisecapture.composeapp.generated.resources.campaign_manage_leave_button
import noisecapture.composeapp.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.model.dao.Campaign
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.dialog.NCDialog
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.components.tertiaryContainerColors
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise

@Composable
fun CampaignManageDialog(
    campaign: Campaign,
    isCurrentCampaign: Boolean,
    onDismissRequest: () -> Unit,
    onLeaveRequest: (Campaign) -> Unit = {},
    onJoinRequest: (Campaign) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    NCDialog(
        title = {
            Column {
                Text(text = campaign.title)
                campaign.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.Noise.four.dark,
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                campaign.description?.let {
                    Text(text = it)
                }

                CampaignDetailsView(campaign)

                Text(
                    text = stringResource(Res.string.campaign_join_disclaimer),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Noise.four.dark,
                )
            }
        },
        confirmButton = {
            if (isCurrentCampaign) {
                NCButton(
                    content = ButtonContent(title = Res.string.campaign_manage_leave_button),
                    colors = Color.Noise.eight.tertiaryContainerColors(hasDropShadow = true),
                    onClick = { onLeaveRequest(campaign) }
                )
            } else {
                NCButton(
                    content = ButtonContent(title = Res.string.campaign_manage_join_button),
                    colors = Color.Noise.four.tertiaryContainerColors(hasDropShadow = true),
                    onClick = { onJoinRequest(campaign) }
                )
            }
        },
        dismissButton = {
            NCButton(
                content = ButtonContent(title = Res.string.cancel),
                colors = Color.Noise.four.transparentContainerColors(),
                onClick = onDismissRequest
            )
        },
        containerColors = Color.Noise.four.secondaryContainerColors(hasDropShadow = true),
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    )
}
