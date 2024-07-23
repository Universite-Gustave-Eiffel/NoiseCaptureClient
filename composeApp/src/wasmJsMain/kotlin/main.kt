import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.koin.core.logger.PrintLogger
import org.noiseplanet.noisecapture.initKoin
import org.noiseplanet.noisecapture.platformModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    // TODO: Add a logger implementation that uses JS console
    val logger = PrintLogger()

    ComposeViewport(document.body!!) {
        initKoin(
            additionalModules = listOf(platformModule)
        ).logger(logger)

        App()
    }
}
