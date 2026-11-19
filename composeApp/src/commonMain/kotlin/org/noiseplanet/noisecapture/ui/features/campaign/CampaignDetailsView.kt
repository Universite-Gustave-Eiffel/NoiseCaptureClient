package org.noiseplanet.noisecapture.ui.features.campaign

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.campaign_details_end_date
import noisecapture.composeapp.generated.resources.campaign_details_invite_code
import noisecapture.composeapp.generated.resources.campaign_details_start_date
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.model.dao.Campaign
import org.noiseplanet.noisecapture.util.DateUtil
import org.noiseplanet.noisecapture.util.millisecondsToFormattedDateTime


@Composable
fun CampaignDetailsView(
    campaign: Campaign,
    modifier: Modifier = Modifier,
    baseStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Text(
        text = buildAnnotatedString {
            campaign.startTimestamp?.let { startTimeStamp ->
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(Res.string.campaign_details_start_date))
                    append(": ")
                }
                append(startTimeStamp.millisecondsToFormattedDateTime(DateUtil.Format.SHORT_DATETIME_FORMAT))
                appendLine()
            }
            campaign.endTimestamp?.let { startTimeStamp ->
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(Res.string.campaign_details_end_date))
                    append(": ")
                }
                append(startTimeStamp.millisecondsToFormattedDateTime(DateUtil.Format.SHORT_DATETIME_FORMAT))
                appendLine()
            }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(stringResource(Res.string.campaign_details_invite_code))
                append(": ")
            }
            append(campaign.code)
        },
        style = baseStyle,
        modifier = modifier,
    )
}
