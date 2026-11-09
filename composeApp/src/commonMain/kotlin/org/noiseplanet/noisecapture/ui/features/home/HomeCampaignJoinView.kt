package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_campaign_join_description
import noisecapture.composeapp.generated.resources.home_campaign_join_enter_code_button
import noisecapture.composeapp.generated.resources.home_campaign_join_scan_button
import noisecapture.composeapp.generated.resources.home_campaign_section_header
import noisecapture.composeapp.generated.resources.qr_code_scanner
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.components.tertiaryContainerColors
import org.noiseplanet.noisecapture.ui.features.campaign.CampaignInviteCodeDialog
import org.noiseplanet.noisecapture.ui.theme.Noise

@Composable
fun HomeCampaignJoinView(
    onCampaignJoinRequest: suspend (inviteCode: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Properties

    var showInviteCodeDialog: Boolean by remember { mutableStateOf(false) }

    val coroutineScope: CoroutineScope = rememberCoroutineScope()


    // - Layout

    Column(modifier = modifier.padding(horizontal = 12.dp)) {
        ListSectionHeader(title = Res.string.home_campaign_section_header)

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(Res.string.home_campaign_join_description),
                style = MaterialTheme.typography.bodySmall,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NCButton(
                    content = ButtonContent(
                        title = Res.string.home_campaign_join_scan_button,
                        icon = Res.drawable.qr_code_scanner,
                    ),
                    colors = Color.Noise.two.secondaryContainerColors(hasDropShadow = true),
                    onClick = {
                        // TODO: Open camera
                    },
                    modifier = Modifier.weight(1f)
                )
                NCButton(
                    content = ButtonContent(title = Res.string.home_campaign_join_enter_code_button),
                    colors = Color.Noise.two.tertiaryContainerColors(),
                    onClick = {
                        showInviteCodeDialog = true
                    }
                )
            }
        }
    }

    if (showInviteCodeDialog) {
        CampaignInviteCodeDialog(
            onDismissRequest = { showInviteCodeDialog = false },
            onCampaignJoinRequest = { inviteCode ->
                coroutineScope.launch {
                    onCampaignJoinRequest(inviteCode)
                    showInviteCodeDialog = false
                }
            },
        )
    }
}
