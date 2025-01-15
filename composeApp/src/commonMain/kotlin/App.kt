import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.noiseplanet.noisecapture.Coordinator

/**
 * Entry point of the Compose app.
 */
@Composable
@Preview
fun App() {
    KoinContext {
        MaterialTheme {
            Coordinator()
        }
    }
}
