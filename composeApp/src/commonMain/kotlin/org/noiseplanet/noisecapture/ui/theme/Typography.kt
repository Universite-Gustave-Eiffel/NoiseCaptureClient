package org.noiseplanet.noisecapture.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
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
fun notoSansTypography() = Typography().run {
    val fontFamily: FontFamily = FontFamily.NotoSans

    copy(
        displayLarge = displayLarge.copy(fontFamily = fontFamily),
        displayMedium = displayMedium.copy(fontFamily = fontFamily),
        displaySmall = displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = titleLarge.copy(fontFamily = fontFamily),
        titleMedium = titleMedium.copy(fontFamily = fontFamily),
        titleSmall = titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = bodySmall.copy(fontFamily = fontFamily),
        labelLarge = labelLarge.copy(fontFamily = fontFamily),
        labelMedium = labelMedium.copy(fontFamily = fontFamily),
        labelSmall = labelSmall.copy(fontFamily = fontFamily),
    )
}


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
