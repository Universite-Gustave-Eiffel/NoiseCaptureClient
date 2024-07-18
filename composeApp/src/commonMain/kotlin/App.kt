import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.noiseplanet.noisecapture.NoiseCaptureApp

/**
 * Entry point of the Compose app.
 */
@Composable
@Preview
fun App() {
    KoinContext {
        MaterialTheme {
            NoiseCaptureApp()
        }
    }
}
