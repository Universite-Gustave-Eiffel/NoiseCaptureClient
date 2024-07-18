import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.logger.PrintLogger
import org.noiseplanet.noisecapture.initKoin

fun MainViewController() = ComposeUIViewController {

    val logger = PrintLogger()
    val koinApplication = initKoin().logger(logger)

    App(koin = koinApplication)
}
