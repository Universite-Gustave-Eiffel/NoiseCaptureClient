import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.noiseplanet.noisecapture.initKoin
import org.noiseplanet.noisecapture.platformModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {

    ComposeViewport(document.body!!) {
        initKoin(
            additionalModules = listOf(
                platformModule
            )
        )
        App()
    }
}
