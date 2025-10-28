package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_map_section_header
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import org.noiseplanet.noisecapture.ui.components.map.MeasurementsMapView


@Composable
fun HomeMapView(
    modifier: Modifier = Modifier,
) {
    // - Properties

    val viewModel: HomeMapViewModel = koinInject()


    // - Layout

    Column(
        modifier = modifier,
    ) {
        ListSectionHeader(title = Res.string.home_map_section_header, paddingTop = 24.dp)

        MeasurementsMapView(
            modifier = Modifier.height(256.dp).clip(MaterialTheme.shapes.medium)
        )
    }
}
