package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.cancel
import noisecapture.composeapp.generated.resources.map_legend_description
import noisecapture.composeapp.generated.resources.map_legend_title
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.transparentContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp


@Composable
fun MapLegendView(
    onDismissRequest: () -> Unit,
) {

    // - Properties

    val items: List<Pair<String, Color>> = NoiseLevelColorRamp.paletteAsLegendElements(
        descendingOrder = true
    )


    // - Layout

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            NCButton(
                onClick = onDismissRequest,
                content = ButtonContent(title = Res.string.cancel),
                colors = Color.Noise.one.transparentContainerColors(),
            )
        },
        title = {
            Text(stringResource(Res.string.map_legend_title))
        },
        text = {
            Column {
                Text(stringResource(Res.string.map_legend_description))

                Spacer(modifier = Modifier.height(16.dp))

                items.forEach { (value, color) ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.height(24.dp).width(32.dp).background(color))
                        Text(value)
                    }
                }
            }
        }
    )
}
