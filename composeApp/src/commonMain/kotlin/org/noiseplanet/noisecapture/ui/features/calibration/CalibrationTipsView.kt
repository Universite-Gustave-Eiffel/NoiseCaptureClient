package org.noiseplanet.noisecapture.ui.features.calibration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.arrow_drop_down
import noisecapture.composeapp.generated.resources.arrow_drop_up
import noisecapture.composeapp.generated.resources.calibration_tips_environment_corners
import noisecapture.composeapp.generated.resources.calibration_tips_environment_direction
import noisecapture.composeapp.generated.resources.calibration_tips_environment_distance
import noisecapture.composeapp.generated.resources.calibration_tips_environment_placement
import noisecapture.composeapp.generated.resources.calibration_tips_environment_quiet
import noisecapture.composeapp.generated.resources.calibration_tips_environment_title
import noisecapture.composeapp.generated.resources.calibration_tips_reference_preferences
import noisecapture.composeapp.generated.resources.calibration_tips_reference_title
import noisecapture.composeapp.generated.resources.calibration_tips_source_nature
import noisecapture.composeapp.generated.resources.calibration_tips_source_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp


@Composable
fun CalibrationTipsView(
    modifier: Modifier = Modifier,
) {
    // - Properties

    val tips = listOf(
        CalibrationTips(
            title = Res.string.calibration_tips_environment_title,
            tips = listOf(
                Res.string.calibration_tips_environment_distance,
                Res.string.calibration_tips_environment_corners,
                Res.string.calibration_tips_environment_direction,
                Res.string.calibration_tips_environment_quiet,
                Res.string.calibration_tips_environment_placement,
            )
        ),
        CalibrationTips(
            title = Res.string.calibration_tips_source_title,
            tips = listOf(Res.string.calibration_tips_source_nature)
        ),
        CalibrationTips(
            title = Res.string.calibration_tips_reference_title,
            tips = listOf(Res.string.calibration_tips_reference_preferences)
        ),
    )


    // - Layout

    Column(
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
    ) {
        for (tipsItem in tips) {
            ExpandableSection(title = stringResource(tipsItem.title)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(12.dp)
                ) {
                    for (tip in tipsItem.tips) {
                        Text(
                            text = stringResource(tip),
                            style = MaterialTheme.typography.bodyMedium,
                            color = NoiseLevelColorRamp.level1Dark,
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ExpandableSectionTitle(
    isExpanded: Boolean,
    title: String,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val icon = if (isExpanded) Res.drawable.arrow_drop_up else Res.drawable.arrow_drop_down


    // - Layout

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.background(NoiseLevelColorRamp.level1Light).padding(12.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = NoiseLevelColorRamp.level1Dark,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = NoiseLevelColorRamp.level1Dark,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun ExpandableSection(
    title: String,
    initiallyExpanded: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // - Properties

    var isExpanded by rememberSaveable { mutableStateOf(initiallyExpanded) }


    // - Layout

    Column(
        modifier = modifier
            .clickable { isExpanded = !isExpanded }
            .fillMaxWidth()
    ) {
        ExpandableSectionTitle(isExpanded = isExpanded, title = title)

        AnimatedVisibility(
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                .fillMaxWidth(),
            visible = isExpanded,
        ) {
            content()
        }
    }
}


data class CalibrationTips(
    val title: StringResource,
    val tips: List<StringResource>,
)
