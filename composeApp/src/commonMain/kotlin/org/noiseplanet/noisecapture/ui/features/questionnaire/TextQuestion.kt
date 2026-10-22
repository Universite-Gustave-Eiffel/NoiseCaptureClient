package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import org.noiseplanet.noisecapture.model.dao.Question
import org.noiseplanet.noisecapture.model.dao.QuestionAnswer
import org.noiseplanet.noisecapture.ui.theme.Noise

@Composable
fun TextQuestion(
    question: Question.Text,
    answer: QuestionAnswer.Text?,
    onAnswerChange: (QuestionAnswer.Text) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    // - Layout

    Column(modifier = modifier) {
        QuestionHeader(question)

        TextField(
            value = answer?.value ?: "",
            onValueChange = { onAnswerChange(QuestionAnswer.Text(question.id, it)) },
            placeholder = {
                question.placeholder?.let {
                    Text(
                        text = question.placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Noise.two.medium,
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Noise.two.light,
                focusedContainerColor = Color.Noise.two.light,
                disabledContainerColor = Color.Noise.two.light,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                errorContainerColor = Color.Noise.eight.light,
                errorTextColor = Color.Noise.eight.dark,
                errorCursorColor = Color.Noise.eight.dark,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
                .alpha(if (enabled) 1.0f else 0.5f),
        )
    }
}
