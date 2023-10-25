import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.bumble.appyx.navigation.integration.DesktopNodeHost
import com.bumble.appyx.shared.root.RootNode
import com.bumble.appyx.shared.ui.theme.AppyxStarterKitTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class Events {
    object OnBackPressed : Events()
}

fun main() = application {
    val events: Channel<Events> = Channel()
    val windowState = rememberWindowState()
    val eventScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    Window(
        state = windowState,
        onCloseRequest = ::exitApplication,
        onKeyEvent = { onKeyEvent(it, events, eventScope) },
    ) {
        AppyxStarterKitTheme {
            DesktopNodeHost(
                windowState = windowState,
                onBackPressedEvents = events.receiveAsFlow().mapNotNull {
                    if (it is Events.OnBackPressed) Unit else null
                }
            ) { buildContext ->
                RootNode(
                    buildContext = buildContext,
                )
            }
        }
    }
}

private fun onKeyEvent(
    keyEvent: KeyEvent,
    events: Channel<Events>,
    coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
): Boolean =
    when {
        keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace -> {
            coroutineScope.launch { events.send(Events.OnBackPressed) }
            true
        }

        else -> false
    }
