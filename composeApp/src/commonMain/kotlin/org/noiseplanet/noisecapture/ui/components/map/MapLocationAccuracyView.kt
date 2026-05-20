package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.location_on
import noisecapture.composeapp.generated.resources.map_gps_accuracy
import noisecapture.composeapp.generated.resources.map_gps_poor_signal
import noisecapture.composeapp.generated.resources.wrong_location
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.services.location.UserLocationService
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import kotlin.math.roundToInt


@Composable
fun MapLocationAccuracyView(
    locationService: UserLocationService = koinInject(),
    modifier: Modifier = Modifier,
) {
    // - Properties

    val locationAccuracy: Double by locationService.liveLocation
        .map { it.horizontalAccuracy }
        .collectAsStateWithLifecycle(initialValue = -1.0)
    val isSignalPoor: Boolean by locationService.isSignalPoor
        .collectAsStateWithLifecycle()

    // Don't render this view if location is unknown
    if (locationAccuracy < 0) return

    val (contentColor, containerColor, icon) = if (isSignalPoor) {
        Triple(
            NoiseLevelColorRamp.level6Dark,
            NoiseLevelColorRamp.level6Light,
            Res.drawable.wrong_location
        )
    } else {
        Triple(
            NoiseLevelColorRamp.level5Dark,
            NoiseLevelColorRamp.level5Light,
            Res.drawable.location_on
        )
    }
    val text = buildAnnotatedString {
        if (isSignalPoor) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${stringResource(Res.string.map_gps_poor_signal)}\n")
            }
        }
        append("${stringResource(Res.string.map_gps_accuracy)}: ${locationAccuracy.roundToInt()}m")
    }


    // - Layout

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color = containerColor, shape = CircleShape)
            .padding(vertical = 8.dp)
            .padding(start = 12.dp, end = 16.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}
