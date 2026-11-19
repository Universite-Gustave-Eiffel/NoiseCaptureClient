package org.noiseplanet.noisecapture.ui.features.campaign

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun QrScannerView(
    onResult: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
) {
    Text(text = "NOT SUPPORTED", modifier = modifier)
}
