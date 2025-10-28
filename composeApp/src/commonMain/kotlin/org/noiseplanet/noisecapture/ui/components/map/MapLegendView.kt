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
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonStyle
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp


@Composable
fun MapLegendView(
    onDismissRequest: () -> Unit,
) {

    // - Properties

    val items: Map<String, Color> = NoiseLevelColorRamp.palette.mapKeys { entry ->
        ">= ${entry.key.toInt()} dB"
    }

    val cancelButtonViewModel = NCButtonViewModel(
        title = Res.string.cancel,
        style = NCButtonStyle.TEXT,
        colors = {
            NCButtonColors.Defaults.text()
        }
    )


    // - Layout

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            NCButton(onClick = onDismissRequest, viewModel = cancelButtonViewModel)
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
