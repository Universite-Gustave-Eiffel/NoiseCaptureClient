package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.model.dao.Question
import org.noiseplanet.noisecapture.model.dao.QuestionAnswer
import org.noiseplanet.noisecapture.ui.theme.Noise

@Composable
fun TextQuestion(
    question: Question.Text,
    value: QuestionAnswer.Text?,
    onValueChange: (QuestionAnswer.Text) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Layout

    Column(modifier = modifier) {
        Text(text = question.title, style = MaterialTheme.typography.titleSmall)
        question.description?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = value?.value ?: "",
            onValueChange = { onValueChange(QuestionAnswer.Text(question.id, it)) },
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
                errorContainerColor = Color.Noise.eight.light,
                errorTextColor = Color.Noise.eight.dark,
                errorCursorColor = Color.Noise.eight.dark,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
