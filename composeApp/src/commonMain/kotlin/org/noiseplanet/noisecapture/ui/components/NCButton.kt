package org.noiseplanet.noisecapture.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.ui.theme.Noise


/**
 * A button component to be used throughout the app for unified styling
 */
@Composable
fun NCButton(
    shape: Shape = NCButtonDefaults.Shape,
    colors: ContainerColors = NCButtonDefaults.colors(),
    textStyle: TextStyle = NCButtonDefaults.textStyle(),
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: ButtonContent,
    onClick: () -> Unit,
    contentPadding: PaddingValues = if (content.icon == null && content.title != null) {
        NCButtonDefaults.TextOnlyContentPadding
    } else if (content.icon != null && content.title == null) {
        NCButtonDefaults.IconOnlyContentPadding
    } else {
        NCButtonDefaults.IconAndTextContentPadding
    },
) {
    Container(
        contentPadding = contentPadding,
        contentAlignment = Alignment.Center,
        colors = colors,
        shape = shape,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content.icon?.let {
                Icon(
                    painterResource(it),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
            content.title?.let {
                Text(stringResource(it), style = textStyle)
            }
        }
    }
}


data class ButtonContent(
    val title: StringResource? = null,
    val icon: DrawableResource? = null,
)


object NCButtonDefaults {

    val TextOnlyContentPadding = PaddingValues(vertical = 12.dp, horizontal = 24.dp)
    val IconOnlyContentPadding = PaddingValues(all = 8.dp)
    val IconAndTextContentPadding = PaddingValues(
        top = 12.dp,
        bottom = 12.dp,
        start = 16.dp,
        end = 24.dp
    )

    val Shape: Shape = CircleShape

    @Composable
    fun colors(): ContainerColors {
        return Color.Noise.one.primaryContainerColors()
    }

    @Composable
    fun textStyle(): TextStyle {
        return MaterialTheme.typography.labelLarge
    }
}
