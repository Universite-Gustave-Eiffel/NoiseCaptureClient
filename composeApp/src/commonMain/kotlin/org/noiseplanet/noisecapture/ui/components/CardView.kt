package org.noiseplanet.noisecapture.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.util.shadow.dropShadow


private const val CORNER_RADIUS: Float = 10f
private const val SHADOW_OFFSET_X: Float = 0f
private const val SHADOW_OFFSET_Y: Float = 4f
private const val SHADOW_BLUR_RADIUS: Float = 12f


/**
 * Wraps the given content into a rounded cornered card.
 * Can be made clickable if given an onClick callback. Clickable cards will display a drop shadow
 * that will be animated when pressed.
 *
 * @param backgroundColor Card's background color
 * @param modifier Base modifier
 * @param onClick Click listener. If provided, card will appear elevated and will animate when pressed.
 * @param content Content block, passed to the internal [Box]
 */
@Composable
fun CardView(
    backgroundColor: Color = Color.White,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(CORNER_RADIUS.dp)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by if (onClick != null) {
        interactionSource.collectIsPressedAsState()
    } else {
        mutableStateOf(true)
    }

    val shadowOffset by animateOffsetAsState(
        targetValue = if (isPressed) {
            Offset.Zero
        } else {
            Offset(SHADOW_OFFSET_X, SHADOW_OFFSET_Y)
        }
    )
    val shadowBlur by animateFloatAsState(
        targetValue = if (isPressed) 0f else SHADOW_BLUR_RADIUS
    )

    var cardModifier = modifier
    onClick?.let {
        cardModifier = modifier.clickable(
            interactionSource,
            indication = null,
            onClick = onClick
        )
    }

    Box(
        modifier = cardModifier
            .dropShadow(shape, offset = shadowOffset, blur = shadowBlur)
            .background(backgroundColor, shape)
            .clip(shape)
            .padding(16.dp),
        contentAlignment = Alignment.TopStart,
        content = content
    )
}
