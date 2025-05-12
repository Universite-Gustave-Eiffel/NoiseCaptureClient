package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_last_measurements_section_header
import org.noiseplanet.noisecapture.model.dao.Measurement
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader


@Composable
fun LastMeasurementsView(
    measurements: List<Measurement>,
    onClickMeasurement: (Measurement) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
) {

    // - Layout

    ListSectionHeader(
        title = Res.string.home_last_measurements_section_header,
        paddingTop = 24.dp,
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        measurements.take(2).forEach { measurement ->
            MeasurementHistoryCardView(
                measurement,
                onClick = onClickMeasurement,
                modifier = modifier.weight(1f)
            )
        }
    }
}
