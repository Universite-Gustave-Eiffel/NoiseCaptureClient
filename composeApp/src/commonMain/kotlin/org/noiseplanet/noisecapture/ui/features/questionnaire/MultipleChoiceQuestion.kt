package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.noiseplanet.noisecapture.model.dao.Question
import org.noiseplanet.noisecapture.model.dao.QuestionAnswer


@Composable
fun MultipleChoiceQuestion(
    question: Question.MultipleChoice,
    answer: QuestionAnswer.MultipleChoice?,
    onAnswerChange: (QuestionAnswer.MultipleChoice) -> Unit,
    modifier: Modifier = Modifier,
) {
    // - Layout

    Column(modifier = modifier) {
        QuestionHeader(question)

        Column(modifier = Modifier.selectableGroup()) {
            question.choices.forEach { choice ->
                val isSelected = answer?.values?.contains(choice.id) ?: false

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .selectable(
                            selected = isSelected,
                            onClick = {
                                val answer = if (isSelected) {
                                    answer.copy(values = answer.values.minus(choice.id))
                                } else {
                                    answer?.copy(values = answer.values.plus(choice.id))
                                        ?: QuestionAnswer.MultipleChoice(
                                            question.id,
                                            setOf(choice.id)
                                        )
                                }
                                onAnswerChange(answer)
                            }
                        ),
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null // null recommended for accessibility with screen readers
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
