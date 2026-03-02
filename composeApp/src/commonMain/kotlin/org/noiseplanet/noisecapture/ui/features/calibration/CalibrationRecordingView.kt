package org.noiseplanet.noisecapture.ui.features.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.calibration_recording_countdown_label
import noisecapture.composeapp.generated.resources.calibration_recording_current_average
import noisecapture.composeapp.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource
import org.koin.core.time.inMs
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.util.AdaptiveUtil
import org.noiseplanet.noisecapture.util.paddingBottomWithInsets
import org.noiseplanet.noisecapture.util.roundTo
import kotlin.math.ceil


@Composable
fun CalibrationRecordingView(
    viewModel: CalibrationScreenViewModel,
    viewState: CalibrationScreenViewModel.ViewState.Recording,
    modifier: Modifier = Modifier,
) = Column(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxSize()
        .widthIn(max = AdaptiveUtil.MAX_FULL_SCREEN_WIDTH)
        .paddingBottomWithInsets(withNavBar = 16.dp, withoutNavBar = 24.dp)
        .padding(top = 16.dp)
        .padding(horizontal = 24.dp),
) {
    Spacer(modifier = Modifier.weight(1f))

    Text(
        text = stringResource(Res.string.calibration_recording_countdown_label),
        style = MaterialTheme.typography.titleMedium,
    )

    Text(
        text = ceil(viewState.timeLeft.inMs / 1000.0).toInt().toString(),
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.Black,
    )

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f),
    ) {
        Text(
            text = stringResource(Res.string.calibration_recording_current_average),
            style = MaterialTheme.typography.titleMedium,
        )

        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(fontSize = MaterialTheme.typography.displayMedium.fontSize)
                ) {
                    append(viewState.currentAverage.roundTo(1).toString())
                }
                append(" dB(A)")
            },
            color = NoiseLevelColorRamp.level1Dark,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
        )
    }

    NCButton(
        viewModel = NCButtonViewModel(
            title = Res.string.cancel,
            colors = {
                NCButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            }
        ),
        onClick = { viewModel.cancelCalibration() },
        modifier = Modifier.height(50.dp).width(200.dp)
    )
}
