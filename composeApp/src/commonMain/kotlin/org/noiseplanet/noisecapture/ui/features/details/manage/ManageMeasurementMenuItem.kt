package org.noiseplanet.noisecapture.ui.features.details.manage

import org.jetbrains.compose.resources.StringResource

data class ManageMeasurementMenuItem(
    val label: StringResource,
    val supportingText: StringResource?,
    val onClick: () -> Unit,
)
