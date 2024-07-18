import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.koin.core.logger.PrintLogger
import org.noiseplanet.noisecapture.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    val logger = PrintLogger()

    ComposeViewport(document.body!!) {
        initKoin().logger(logger)
        App()
    }
}
