package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterView


@Composable
fun SoundLevelMeterHeaderView(
    viewModel: HomeScreenViewModel,
    onClickOpenSoundLevelMeterButton: () -> Unit,
    showPermissionPrompt: (Permission) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val sizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isCompact = sizeClass.minWidthDp < WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND

    val shape = if (isCompact) {
        MaterialTheme.shapes.large.copy(
            topStart = CornerSize(0),
            topEnd = CornerSize(0),
        )
    } else {
        MaterialTheme.shapes.large
    }


    // - Layout

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier.clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClickOpenSoundLevelMeterButton)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            SoundLevelMeterView(showPermissionPrompt = showPermissionPrompt)

            if (isCompact) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = stringResource(viewModel.soundLevelMeterHintText),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.alpha(0.75f)
                    )

                    NCButton(
                        onClick = onClickOpenSoundLevelMeterButton,
                        viewModel = viewModel.soundLevelMeterButtonViewModel,
                        modifier = Modifier.height(50.dp)
                            .fillMaxWidth()
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(viewModel.soundLevelMeterHintText),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.alpha(0.75f).weight(1f)
                    )

                    NCButton(
                        onClick = onClickOpenSoundLevelMeterButton,
                        viewModel = viewModel.soundLevelMeterButtonViewModel,
                        modifier = Modifier.height(50.dp)
                    )
                }
            }
        }
    }
}
