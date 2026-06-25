package org.noiseplanet.noisecapture.ui.features.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.home_map_browse_button
import noisecapture.composeapp.generated.resources.home_map_section_header
import noisecapture.composeapp.generated.resources.map
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.Container
import org.noiseplanet.noisecapture.ui.components.ListSectionHeader
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.map.MapView
import org.noiseplanet.noisecapture.ui.components.secondaryContainerColors
import org.noiseplanet.noisecapture.ui.components.tertiaryContainerColors
import org.noiseplanet.noisecapture.ui.navigation.router.HomeRouter
import org.noiseplanet.noisecapture.ui.theme.Noise


@Composable
fun HomeMapView(
    router: HomeRouter,
    modifier: Modifier = Modifier,
) {
    // - Layout

    Column(
        modifier = modifier,
    ) {
        ListSectionHeader(
            title = Res.string.home_map_section_header,
            modifier = Modifier.padding(start = 12.dp),
        )

        Container(
            contentAlignment = Alignment.BottomEnd,
            contentPadding = PaddingValues(0.dp),
            shape = MaterialTheme.shapes.large,
            onClick = router::onClickOpenMapButton,
            colors = Color.Noise.one.secondaryContainerColors(hasDropShadow = true),
        ) {
            MapView(modifier = Modifier.clip(MaterialTheme.shapes.large))

            Box(modifier = Modifier.fillMaxSize())

            NCButton(
                content = ButtonContent(
                    icon = Res.drawable.map,
                    title = Res.string.home_map_browse_button,
                ),
                colors = Color.Noise.one.tertiaryContainerColors(hasDropShadow = true),
                onClick = router::onClickOpenMapButton,
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
            )
        }
    }
}
