import androidx.compose.runtime.getValue
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.compose.koinInject
import org.noiseplanet.noisecapture.initKoin
import org.noiseplanet.noisecapture.platformModule
import org.noiseplanet.noisecapture.util.FilePickerEvent
import org.noiseplanet.noisecapture.util.IOSFilePickerEventBus
import platform.UIKit.UIDocumentInteractionController
import platform.UIKit.UIDocumentInteractionControllerDelegateProtocol
import platform.darwin.NSObject

/**
 * iOS application entry point
 */
@OptIn(ExperimentalForeignApi::class)
@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController {

    // - DI

    initKoin(
        additionalModules = listOf(
            platformModule
        )
    )


    // - Properties

    // Listen for file picker events and present view controller accordingly.
    val eventBus: IOSFilePickerEventBus = koinInject()
    val event: FilePickerEvent? by eventBus.events.collectAsStateWithLifecycle()

    // Hold a reference to the ViewController and its delegate while it is being presented
    var documentInteractionController: UIDocumentInteractionController?
    var documentInteractionDelegate: UIDocumentInteractionControllerDelegate?

    event?.let { event ->
        val viewController = LocalUIViewController.current
        // Create and configure document interaction controller
        documentInteractionDelegate = UIDocumentInteractionControllerDelegate {
            // Drop reference to document interaction controller
            documentInteractionController = null
            event.onDismiss()
        }

        documentInteractionController = UIDocumentInteractionController
            .interactionControllerWithURL(event.fileUrl)
        documentInteractionController?.delegate = documentInteractionDelegate
        documentInteractionController?.presentOptionsMenuFromRect(
            rect = viewController.view.bounds,
            inView = viewController.view,
            animated = true
        )
    }


    // - Entry point

    App()
}


private class UIDocumentInteractionControllerDelegate(
    private val onDismiss: () -> Unit,
) : NSObject(), UIDocumentInteractionControllerDelegateProtocol {

    override fun documentInteractionControllerDidDismissOptionsMenu(
        controller: UIDocumentInteractionController,
    ) {
        onDismiss()
    }
}
