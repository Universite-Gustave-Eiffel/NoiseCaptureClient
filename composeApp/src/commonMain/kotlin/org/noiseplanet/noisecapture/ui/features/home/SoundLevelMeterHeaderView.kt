package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_slm_button_title
import noisecapture.composeapp.generated.resources.mic
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterView


@Composable
fun SoundLevelMeterHeaderView(
    onClickOpenSoundLevelMeterButton: () -> Unit,
    showPermissionPrompt: (Permission) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val sizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
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
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            SoundLevelMeterView(showPermissionPrompt = showPermissionPrompt)

            NCButton(
                onClick = onClickOpenSoundLevelMeterButton,
                content = ButtonContent(
                    title = Res.string.home_slm_button_title,
                    icon = Res.drawable.mic
                ),
                modifier = Modifier.height(50.dp).widthIn(max = 300.dp).fillMaxWidth()
            )
        }
    }
}
