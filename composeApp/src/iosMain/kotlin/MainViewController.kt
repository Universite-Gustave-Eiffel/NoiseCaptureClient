import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.logger.PrintLogger
import org.noiseplanet.noisecapture.initKoin

/**
 * iOS application entry point
 */
fun MainViewController() = ComposeUIViewController {

    val logger = PrintLogger()

    initKoin().logger(logger)
    App()
}
