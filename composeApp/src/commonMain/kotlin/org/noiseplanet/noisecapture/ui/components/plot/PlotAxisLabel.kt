package org.noiseplanet.noisecapture.ui.components.plot

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign


@Composable
fun PlotAxisLabel(
    text: String,
    textAlign: TextAlign = TextAlign.Unspecified,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        textAlign = textAlign,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = modifier,
    )
}
