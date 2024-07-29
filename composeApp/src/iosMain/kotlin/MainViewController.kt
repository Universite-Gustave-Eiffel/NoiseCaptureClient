import androidx.compose.ui.window.ComposeUIViewController
import org.noiseplanet.noisecapture.initKoin
import org.noiseplanet.noisecapture.platformModule

/**
 * iOS application entry point
 */
@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController {

    initKoin(
        additionalModules = listOf(
            platformModule
        )
    )
    App()
}
