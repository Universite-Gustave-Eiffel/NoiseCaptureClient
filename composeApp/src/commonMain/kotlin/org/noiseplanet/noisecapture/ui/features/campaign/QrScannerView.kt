package org.noiseplanet.noisecapture.ui.features.campaign

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun QrScannerView(
    onResult: (String) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
)
