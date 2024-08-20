package org.noiseplanet.noisecapture.ui.features.settings.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.koin.compose.koinInject

@Composable
fun AboutScreen(
    viewModel: AboutScreenViewModel = koinInject(),
) {
    Text(
        viewModel.placeholder,
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxSize()
    )
}
