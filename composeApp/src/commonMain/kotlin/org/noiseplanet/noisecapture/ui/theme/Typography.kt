package org.noiseplanet.noisecapture.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownTypography
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.noto_sans_mono_variable
import noisecapture.composeapp.generated.resources.noto_sans_variable
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource


@Composable
private fun variableFontFamily(fontResource: FontResource) = FontFamily(
    listOf(
        FontWeight.W100,
        FontWeight.W200,
        FontWeight.W300,
        FontWeight.W400,
        FontWeight.W500,
        FontWeight.W600,
        FontWeight.W700,
        FontWeight.W800,
        FontWeight.W900,
    ).map { fontWeight ->
        Font(
            fontResource,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(fontWeight.weight)
            ),
            weight = fontWeight,
        )
    }
)


val FontFamily.Companion.NotoSans: FontFamily
    @Composable
    get() = variableFontFamily(Res.font.noto_sans_variable)


val FontFamily.Companion.NotoSansMono: FontFamily
    @Composable
    get() = variableFontFamily(Res.font.noto_sans_mono_variable)


@Composable
fun noiseCaptureTypography() = Typography().run {
    val fontFamily: FontFamily = FontFamily.NotoSans

    copy(
        displayLarge = displayLarge.copy(
            fontFamily = FontFamily.NotoSansMono,
            fontWeight = FontWeight.Black,
        ),
        displayMedium = displayMedium.copy(
            fontFamily = FontFamily.NotoSansMono,
            fontWeight = FontWeight.SemiBold,
        ),
        displaySmall = displaySmall.copy(
            fontFamily = FontFamily.NotoSansMono,
            fontWeight = FontWeight.Bold,
        ),

        headlineLarge = headlineLarge.copy(
            fontFamily = FontFamily.NotoSansMono,
            fontWeight = FontWeight.Black,
        ),
        headlineMedium = headlineMedium.copy(
            fontFamily = FontFamily.NotoSansMono,
            fontWeight = FontWeight.SemiBold,
        ),
        headlineSmall = headlineSmall.copy(
            fontFamily = FontFamily.NotoSansMono,
            fontWeight = FontWeight.Bold,
        ),

        titleLarge = titleLarge.copy(
            fontFamily = FontFamily.NotoSans,
            fontWeight = FontWeight.SemiBold,
        ),
        titleMedium = titleMedium.copy(
            fontFamily = FontFamily.NotoSans,
            fontWeight = FontWeight.SemiBold,
        ),
        titleSmall = titleSmall.copy(
            fontFamily = FontFamily.NotoSans,
            fontWeight = FontWeight.Bold,
        ),

        bodyLarge = bodyLarge.copy(
            fontFamily = FontFamily.NotoSans,
            fontWeight = FontWeight.Medium,
        ),
        bodyMedium = bodyMedium.copy(
            fontFamily = FontFamily.NotoSans,
            fontWeight = FontWeight.Normal,
        ),
        bodySmall = bodySmall.copy(
            fontFamily = FontFamily.NotoSans,
            fontWeight = FontWeight.Normal,
        ),

        labelLarge = labelLarge.copy(
            fontFamily = FontFamily.NotoSans,
            fontWeight = FontWeight.SemiBold,
        ),
        labelMedium = labelMedium.copy(
            fontFamily = FontFamily.NotoSans,
            fontWeight = FontWeight.Medium,
        ),
        labelSmall = labelSmall.copy(
            fontFamily = FontFamily.NotoSans,
            fontWeight = FontWeight.Medium,
        ),
    )
}

val Typography.titleMono: TextStyle
    @Composable get() = noiseCaptureTypography().titleMedium.copy(
        fontFamily = FontFamily.NotoSansMono,
        fontWeight = FontWeight.ExtraBold,
    )


@Composable
fun defaultMarkdownTypography(): MarkdownTypography = markdownTypography(
    h1 = MaterialTheme.typography.displayLarge,
    h2 = MaterialTheme.typography.displayMedium,
    h3 = MaterialTheme.typography.displaySmall,
    h4 = MaterialTheme.typography.headlineMedium,
    h5 = MaterialTheme.typography.headlineSmall,
    h6 = MaterialTheme.typography.titleLarge,
    text = MaterialTheme.typography.bodyMedium,
    code = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
    inlineCode = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
    quote = MaterialTheme.typography.bodyMedium.plus(SpanStyle(fontStyle = FontStyle.Italic)),
    paragraph = MaterialTheme.typography.bodyMedium,
    ordered = MaterialTheme.typography.bodyMedium,
    bullet = MaterialTheme.typography.bodyMedium,
    list = MaterialTheme.typography.bodyMedium,
    textLink = TextLinkStyles(
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
        ).toSpanStyle()
    ),
)
