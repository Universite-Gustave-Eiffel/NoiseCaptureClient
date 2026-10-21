package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.map_legend_title
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.theme.Neutral
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp


@Composable
fun MapLegendView() {

    // - Properties

    val items: List<Pair<String, Color>> = NoiseLevelColorRamp.paletteAsLegendElements(
        descendingOrder = true
    )


    // - Layout

    Column(
        modifier = Modifier.padding(16.dp)
            .padding(top = 24.dp)
    ) {
        Text(
            text = stringResource(Res.string.map_legend_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(IntrinsicSize.Max)
                .padding(bottom = 8.dp)
        ) {
            Text(
                text = "Color",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color.Neutral.medium,
            )
            Text(
                text = "dB(A)",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Neutral.medium,
            )
        }

        items.forEach { (value, color) ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                Box(modifier = Modifier.height(24.dp).width(32.dp).background(color))
                Text(value, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
