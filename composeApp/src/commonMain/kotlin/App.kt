import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.noiseplanet.noisecapture.NoiseCaptureApp

@Composable
@Preview
fun App() {
    MaterialTheme {
        NoiseCaptureApp()
    }
}