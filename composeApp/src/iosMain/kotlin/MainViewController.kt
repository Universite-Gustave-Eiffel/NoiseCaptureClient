import androidx.compose.runtime.Composable
import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.logger.PrintLogger
import org.noiseplanet.noisecapture.initKoin

/**
 * iOS application entry point
 */
@Composable
fun MainViewController() = ComposeUIViewController {

    // TODO: Platform specific implementation
    val logger = PrintLogger()

    initKoin().logger(logger)
    App()
}
