package org.noiseplanet.noisecapture.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

data class MenuItem(
    val label: String,
    val imageVector: ImageVector,
    val onClick: () -> Unit,
)
