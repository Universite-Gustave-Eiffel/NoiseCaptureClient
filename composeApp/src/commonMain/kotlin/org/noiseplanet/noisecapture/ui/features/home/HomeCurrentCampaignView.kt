package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_campaign_manage_button
import noisecapture.composeapp.generated.resources.home_campaign_section_header
import org.noiseplanet.noisecapture.model.dao.Campaign
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.Container
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.components.tertiaryContainerColors
import org.noiseplanet.noisecapture.ui.features.campaign.CampaignDetailsView
import org.noiseplanet.noisecapture.ui.theme.Noise

@Composable
fun HomeCurrentCampaignView(
    campaign: Campaign,
    onManageCampaignRequest: (Campaign) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Layout

    Column(modifier = modifier) {
        ListSectionHeader(
            title = Res.string.home_campaign_section_header,
            modifier = Modifier.padding(start = 12.dp)
        )

        Container(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            colors = Color.Noise.four.secondaryContainerColors().copy(
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column {
                Text(text = campaign.title, style = MaterialTheme.typography.titleMedium)
                campaign.subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Noise.four.dark,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CampaignDetailsView(
                        campaign, baseStyle = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )

                    NCButton(
                        content = ButtonContent(title = Res.string.home_campaign_manage_button),
                        colors = Color.Noise.four.tertiaryContainerColors(hasDropShadow = true),
                        onClick = { onManageCampaignRequest(campaign) }
                    )
                }
            }
        }
    }
}
