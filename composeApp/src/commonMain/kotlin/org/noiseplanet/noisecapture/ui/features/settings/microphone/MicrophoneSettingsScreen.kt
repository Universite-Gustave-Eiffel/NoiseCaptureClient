package org.noiseplanet.noisecapture.ui.features.settings.microphone

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.koin.compose.koinInject


@Composable
fun MicrophoneSettingsScreen(
    viewModel: MicrophoneSettingsScreenViewModel = koinInject(),
) {
    Text(
        viewModel.placeholder,
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxSize()
    )
}
