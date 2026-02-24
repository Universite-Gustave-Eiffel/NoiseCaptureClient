package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_mic_not_calibrated
import noisecapture.composeapp.generated.resources.home_mic_setup_section_header
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.micselect.MicrophoneSelectView
import org.noiseplanet.noisecapture.ui.navigation.router.HomeRouter
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp


@Composable
fun HomeMicrophoneSetupView(
    router: HomeRouter,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val viewModel: HomeMicrophoneSetupViewModel = koinViewModel()


    // - Layout

    // TODO: If permission isn't granted, show a placeholder message

    Column(modifier = modifier) {
        ListSectionHeader(
            title = Res.string.home_mic_setup_section_header,
            modifier = Modifier.padding(start = 12.dp),
        )

        Column {
            MicrophoneSelectView(
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier
                    .clip(
                        MaterialTheme.shapes.large.copy(
                            bottomStart = ZeroCornerSize,
                            bottomEnd = ZeroCornerSize,
                        )
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(IntrinsicSize.Min)
                    .background(
                        color = NoiseLevelColorRamp.level6Light,
                        shape = MaterialTheme.shapes.large.copy(
                            topStart = ZeroCornerSize,
                            topEnd = ZeroCornerSize
                        )
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(Res.string.home_mic_not_calibrated),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoiseLevelColorRamp.level6Dark,
                    modifier = Modifier.weight(1f),
                )

                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NoiseLevelColorRamp.level6Dark,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    NCButton(
                        viewModel = viewModel.calibrationButtonViewModel,
                        onClick = router::onClickCalibrateButton,
                    )
                }
            }
        }
    }
}
