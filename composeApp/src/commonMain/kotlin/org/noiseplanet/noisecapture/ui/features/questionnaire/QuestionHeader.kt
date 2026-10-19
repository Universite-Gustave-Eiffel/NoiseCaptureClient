package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.model.dao.Question
import org.noiseplanet.noisecapture.ui.theme.Noise

/**
 * Question header component that displays title, optional description, and an indicator of whether
 * the question must be answered.
 *
 * @param question Question
 * @param modifier Modifier
 */
@Composable
fun QuestionHeader(
    question: Question,
    modifier: Modifier = Modifier,
) {
    // - Layout

    Column(modifier = modifier.padding(bottom = 8.dp)) {
        Text(
            text = buildAnnotatedString {
                append(question.title)
                if (question.required) {
                    withStyle(style = SpanStyle(color = Color.Noise.eight.medium)) {
                        append(" *")
                    }
                }
            },
            style = MaterialTheme.typography.titleSmall
        )

        question.description?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }
    }
}
