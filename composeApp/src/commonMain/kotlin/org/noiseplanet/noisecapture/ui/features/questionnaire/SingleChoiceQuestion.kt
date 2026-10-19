package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.model.dao.Question
import org.noiseplanet.noisecapture.model.dao.QuestionAnswer


@Composable
fun SingleChoiceQuestion(
    question: Question.SingleChoice,
    value: QuestionAnswer.SingleChoice?,
    onValueChange: (QuestionAnswer.SingleChoice) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Layout

    Column(modifier = modifier) {
        Text(text = question.title, style = MaterialTheme.typography.titleSmall)
        question.description?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.selectableGroup()) {
            question.choices.forEach { choice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = value?.value == choice.id,
                            onClick = {
                                onValueChange(QuestionAnswer.SingleChoice(question.id, choice.id))
                            }
                        ),
                ) {
                    RadioButton(
                        selected = value?.value == choice.id,
                        onClick = null // null recommended for accessibility with screen readers
                    )
                    Text(
                        text = choice.title,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
