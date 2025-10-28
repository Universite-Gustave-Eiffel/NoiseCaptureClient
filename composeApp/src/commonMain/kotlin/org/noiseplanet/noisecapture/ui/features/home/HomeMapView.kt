package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_map_browse_button
import noisecapture.composeapp.generated.resources.home_map_section_header
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import org.noiseplanet.noisecapture.ui.components.button.NCButton
import org.noiseplanet.noisecapture.ui.components.button.NCButtonColors
import org.noiseplanet.noisecapture.ui.components.button.NCButtonIconPlacement
import org.noiseplanet.noisecapture.ui.components.button.NCButtonViewModel
import org.noiseplanet.noisecapture.ui.components.map.MeasurementsMapView
import org.noiseplanet.noisecapture.ui.navigation.router.HomeRouter


@Composable
fun HomeMapView(
    router: HomeRouter,
    modifier: Modifier = Modifier,
) {
    // - Properties

    val browseButtonViewModel = remember {
        NCButtonViewModel(
            title = Res.string.home_map_browse_button,
            icon = Icons.Default.Map,
            iconPlacement = NCButtonIconPlacement.END,
            hasDropShadow = true,
            colors = {
                NCButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
            },
        )
    }


    // - Layout

    Column(
        modifier = modifier,
    ) {
        ListSectionHeader(title = Res.string.home_map_section_header, paddingTop = 24.dp)

        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.aspectRatio(1.5f).clip(MaterialTheme.shapes.medium)
        ) {
            MeasurementsMapView(
                modifier = Modifier.fillMaxSize()
            )

            Box(modifier = Modifier.fillMaxSize().clickable(onClick = router::onClickOpenMapButton))

            NCButton(
                viewModel = browseButtonViewModel,
                onClick = router::onClickOpenMapButton,
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp).height(50.dp)
            )
        }
    }
}
