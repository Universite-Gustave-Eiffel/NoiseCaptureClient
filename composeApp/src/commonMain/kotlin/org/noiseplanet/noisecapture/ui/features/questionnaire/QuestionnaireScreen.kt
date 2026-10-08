package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.previous
import org.noiseplanet.noisecapture.model.dao.Question
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.Container
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.OptionalSlider
import org.noiseplanet.noisecapture.ui.components.tertiaryContainerColors
import org.noiseplanet.noisecapture.ui.theme.Noise
import org.noiseplanet.noisecapture.ui.theme.titleMono

@Composable
fun QuestionnaireScreen(
    viewModel: QuestionnaireScreenViewModel,
) {
    // - Properties

    val viewState: QuestionnaireScreenViewModel.ViewState? by viewModel.viewState.collectAsStateWithLifecycle()


    // - Layout

    Surface {
        when (viewState) {
            is QuestionnaireScreenViewModel.ViewState.QuestionnaireState -> {
                val state = viewState as QuestionnaireScreenViewModel.ViewState.QuestionnaireState
                Column(
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier.verticalScroll(state = rememberScrollState())
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                ) {
                    val page = state.questionnaire.pages[state.currentPageIndex]

                    Container {
                        Column {
                            Text(
                                text = "${state.currentPageIndex + 1}/${state.questionnaire.pages.size}",
                                style = MaterialTheme.typography.titleMono,
                                color = Color.Noise.three.dark
                            )
                            Text(text = page.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = page.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    for (question in page.questions) {
                        SliderQuestion(question)
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (state.currentPageIndex > 0) {
                            NCButton(
                                content = ButtonContent(title = Res.string.previous),
                                colors = Color.Noise.one.tertiaryContainerColors(),
                                onClick = { viewModel.previousPage() },
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        NCButton(
                            content = state.confirmButtonContent,
                            onClick = { viewModel.confirmPage() }
                        )
                    }
                }
            }

            else -> return@Surface
        }
    }
}

@Composable
fun SliderQuestion(
    question: Question,
) {
    // - Properties

    var value: Float? by remember { mutableStateOf(null) }


    // - Layout

    Column {
        Text(text = question.title, style = MaterialTheme.typography.titleSmall)
        question.description?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }

        OptionalSlider(
            value = value,
            onValueChange = { value = it },
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
