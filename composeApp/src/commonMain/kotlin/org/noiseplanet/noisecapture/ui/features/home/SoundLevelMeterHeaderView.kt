package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.permission.Permission
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterView


@Composable
fun SoundLevelMeterHeaderView(
    viewModel: HomeScreenViewModel,
    onClickOpenSoundLevelMeterButton: () -> Unit,
    showPermissionPrompt: (Permission) -> Unit,
) {
    // - Properties

    val shape = MaterialTheme.shapes.large.copy(
        topStart = CornerSize(0),
        topEnd = CornerSize(0),
    )


    // - Layout

    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer, shape)
    ) {
        SoundLevelMeterView(showPermissionPrompt = showPermissionPrompt)

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 16.dp,
                start = 16.dp,
                end = 16.dp
            )
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
    }
}
