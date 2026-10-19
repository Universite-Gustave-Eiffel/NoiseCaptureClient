package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.model.dao.Question
import org.noiseplanet.noisecapture.model.dao.QuestionAnswer
import org.noiseplanet.noisecapture.ui.components.OptionalSlider


@Composable
fun SliderQuestion(
    question: Question.Slider,
    value: QuestionAnswer.Slider?,
    onValueChange: (QuestionAnswer.Slider) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Layout

    Column(modifier = modifier) {
        Text(text = question.title, style = MaterialTheme.typography.titleSmall)
        question.description?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OptionalSlider(
            value = value?.value,
            onValueChange = { onValueChange(QuestionAnswer.Slider(question.id, it)) },
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = question.startLabel, style = MaterialTheme.typography.bodySmall)
            Text(text = question.endLabel, style = MaterialTheme.typography.bodySmall)
        }
    }
}
