package org.noiseplanet.noisecapture.ui.features.home

import Platform
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.log.Logger
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.spl.SoundLevelMeterView

/**
 * Home screen layout.
 */
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
) {
    val logger: Logger = koinInject()
    val platform: Platform = koinInject()
    logger.info(platform.userAgent.toString())

    // - Layout

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column {
            SoundLevelMeterHeaderView(viewModel)

            // TODO: Add last measurements section

            // TODO: Add device calibration section

            // TODO: Add more info section
        }
    }
}


@Composable
private fun SoundLevelMeterHeaderView(
    viewModel: HomeScreenViewModel,
) {
    Column(
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        SoundLevelMeterView(viewModel.soundLevelMeterViewModel)

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
                text = stringResource(viewModel.hintText),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.75f)
            )

            NCButton(
                viewModel = viewModel.soundLevelMeterButtonViewModel,
                modifier = Modifier.height(50.dp)
                    .fillMaxWidth()
            )
        }
    }
}
