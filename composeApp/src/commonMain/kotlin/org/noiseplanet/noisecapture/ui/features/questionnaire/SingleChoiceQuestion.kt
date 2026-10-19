package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
    answer: QuestionAnswer.SingleChoice?,
    onAnswerChange: (QuestionAnswer.SingleChoice) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Layout

    Column(modifier = modifier) {
        QuestionHeader(question)

        Column(modifier = Modifier.selectableGroup()) {
            question.choices.forEach { choice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .selectable(
                            selected = answer?.value == choice.id,
                            onClick = {
                                onAnswerChange(QuestionAnswer.SingleChoice(question.id, choice.id))
                            }
                        ),
                ) {
                    RadioButton(
                        selected = answer?.value == choice.id,
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
