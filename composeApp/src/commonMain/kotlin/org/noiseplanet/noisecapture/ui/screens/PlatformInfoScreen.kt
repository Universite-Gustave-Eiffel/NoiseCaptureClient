package org.noiseplanet.noisecapture.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.noiseplanet.noisecapture.Greeting

/**
 * Gives information about the platform the app is currently running on.
 * Not aimed to be kept in the end but for now serves as a practical example of
 * platform specific implementations
 */
@Composable
fun PlatformInfoScreen(
    modifier: Modifier = Modifier,
) {
    // A platform specific greeting message
    val greeting = remember { Greeting().greet() }

    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painterResource(Res.drawable.compose_multiplatform), null)
        Text("Compose: $greeting")
    }
}
