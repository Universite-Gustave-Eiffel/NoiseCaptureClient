package org.noiseplanet.noisecapture.ui.components.button

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


/**
 * Colors for an [NCButton].
 *
 * @param containerColor Background color. For [NCButtonStyle.OUTLINED], will be used as border color.
 * For [NCButtonStyle.TEXT], will be ignored.
 * @param contentColor Text and icon color.
 */
class NCButtonColors(
    val containerColor: Color,
    val contentColor: Color,
) {

    // - Constants

    object Defaults {

        @Composable
        fun primary(): NCButtonColors {
            return ButtonDefaults.buttonColors().let {
                NCButtonColors(
                    containerColor = it.containerColor,
                    contentColor = it.contentColor,
                )
            }
        }

        @Composable
        fun secondary(): NCButtonColors {
            return ButtonDefaults.filledTonalButtonColors().let {
                NCButtonColors(
                    containerColor = it.containerColor,
                    contentColor = it.contentColor,
                )
            }
        }

        @Composable
        fun outlined(): NCButtonColors {
            return ButtonDefaults.outlinedButtonColors().let {
                NCButtonColors(
                    containerColor = it.contentColor,
                    contentColor = it.contentColor,
                )
            }
        }

        @Composable
        fun text(): NCButtonColors {
            return ButtonDefaults.textButtonColors().let {
                NCButtonColors(
                    containerColor = it.contentColor,
                    contentColor = it.contentColor,
                )
            }
        }
    }


    // - Public functions

    @Composable
    fun toButtonColors(): ButtonColors {
        return ButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f),
        )
    }

    @Composable
    fun toIconButtonColors(): IconButtonColors {
        return IconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f),
        )
    }
}
