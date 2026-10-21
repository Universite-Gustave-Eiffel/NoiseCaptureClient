package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import org.noiseplanet.noisecapture.model.dao.Question
import org.noiseplanet.noisecapture.model.dao.QuestionAnswer
import org.noiseplanet.noisecapture.ui.components.OptionalSlider


@Composable
fun SliderQuestion(
    question: Question.Slider,
    answer: QuestionAnswer.Slider?,
    onAnswerChange: (QuestionAnswer.Slider) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    // - Layout

    Column(modifier = modifier) {
        QuestionHeader(question)

        OptionalSlider(
            value = answer?.value,
            onValueChange = { onAnswerChange(QuestionAnswer.Slider(question.id, it)) },
            enabled = enabled,
            modifier = Modifier.alpha(if (enabled) 1.0f else 0.5f),
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = question.startLabel, style = MaterialTheme.typography.labelMedium)
            Text(text = question.endLabel, style = MaterialTheme.typography.labelMedium)
        }
    }
}
