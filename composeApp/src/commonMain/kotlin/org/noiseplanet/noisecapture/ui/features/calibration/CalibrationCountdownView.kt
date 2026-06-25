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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.calibration_countdown_label
import noisecapture.composeapp.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource
import org.koin.core.time.inMs
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.tertiaryContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise
import org.noiseplanet.noisecapture.util.AdaptiveUtil
import org.noiseplanet.noisecapture.util.paddingBottomWithInsets
import kotlin.math.ceil


@Composable
fun CalibrationCountdownView(
    viewModel: CalibrationScreenViewModel,
    viewState: CalibrationScreenViewModel.ViewState.Countdown,
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
        text = stringResource(Res.string.calibration_countdown_label),
        style = MaterialTheme.typography.titleMedium,
    )

    Text(
        text = ceil(viewState.timeLeft.inMs / 1000.0).toInt().toString(),
        style = MaterialTheme.typography.displayLarge,
        fontWeight = FontWeight.Black,
    )

    Spacer(modifier = Modifier.weight(1f))

    NCButton(
        content = ButtonContent(title = Res.string.cancel),
        colors = Color.Noise.one.tertiaryContainerColors(),
        onClick = { viewModel.cancelCalibration() },
        modifier = Modifier.height(50.dp).width(200.dp)
    )
}
