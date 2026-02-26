package org.noiseplanet.noisecapture.model.dao

import kotlinx.serialization.Serializable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.microphone_type_bluetooth
import noisecapture.composeapp.generated.resources.microphone_type_builtin
import noisecapture.composeapp.generated.resources.microphone_type_unknown
import noisecapture.composeapp.generated.resources.microphone_type_wired_aux
import noisecapture.composeapp.generated.resources.microphone_type_wired_usb
import org.jetbrains.compose.resources.StringResource


/**
 * Cross-platform representation of a microphone input source.
 *
 * @param id Unique device identifier. Provided by the system, might not be consistent if external
 *           microphone is unplugged, then plugged again.
 * @param label Microphone name, provided by the system. For wired Aux/USB devices, the name might
 *             be the same regardless of the actually plugged microphone.
 * @param type Microphone type (builtin, wired, bluetooth, ...). We expect only one microphone of
 *             each type to be available at the same time.
 */
@Serializable
data class MicrophoneInfo(
    val id: String,
    val label: String,
    val type: MicrophoneType,
)


/**
 * Possible supported microphone types
 */
@Serializable
enum class MicrophoneType {

    // - Cases

    /**
     * The device's internal microphone(s)
     */
    BUILTIN,

    /**
     * External microphone plugged into the aux input of the phone
     */
    WIRED_AUX,

    /**
     * External microphone plugged into the USB input of the phone
     */
    WIRED_USB,

    /**
     * External wireless microphone connected via bluetooth
     */
    BLUETOOTH,

    /**
     * Unsupported microphone type
     */
    UNKNOWN;


    // - Properties

    /**
     * Localized name for this microphone type.
     */
    val displayName: StringResource
        get() = when (this) {
            BUILTIN -> Res.string.microphone_type_builtin
            WIRED_AUX -> Res.string.microphone_type_wired_aux
            WIRED_USB -> Res.string.microphone_type_wired_usb
            BLUETOOTH -> Res.string.microphone_type_bluetooth
            UNKNOWN -> Res.string.microphone_type_unknown
        }
}
