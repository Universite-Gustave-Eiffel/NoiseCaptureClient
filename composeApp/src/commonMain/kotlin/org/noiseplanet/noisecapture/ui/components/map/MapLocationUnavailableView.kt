package org.noiseplanet.noisecapture.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.location_off
import noisecapture.composeapp.generated.resources.map_location_services_unavailable_description
import noisecapture.composeapp.generated.resources.map_location_services_unavailable_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.theme.Noise


@Composable
fun MapLocationUnavailableView(
    modifier: Modifier = Modifier,
) {
    // - Layout

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.background(color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 40.dp)
            .padding(bottom = 80.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.location_off),
            contentDescription = null,
            tint = Color.Noise.eight.medium,
            modifier = Modifier.size(64.dp).padding(bottom = 12.dp)
        )

        Column(
            modifier = Modifier.widthIn(max = 500.dp)
        ) {
            Text(
                text = stringResource(Res.string.map_location_services_unavailable_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            )

            Text(
                text = stringResource(Res.string.map_location_services_unavailable_description),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
