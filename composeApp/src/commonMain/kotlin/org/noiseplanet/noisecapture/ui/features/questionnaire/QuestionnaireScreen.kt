package org.noiseplanet.noisecapture.ui.features.questionnaire

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.noiseplanet.noisecapture.model.dao.Question
import org.noiseplanet.noisecapture.ui.components.OptionalSlider

@Composable
fun QuestionnaireScreen(
    viewModel: QuestionnaireScreenViewModel,
) {
    // - Properties

    val viewState: QuestionnaireScreenViewModel.ViewState? by viewModel.viewState.collectAsStateWithLifecycle()


    // - Layout

    Surface {
        when (viewState) {
            is QuestionnaireScreenViewModel.ViewState.Loading -> return@Surface
            is QuestionnaireScreenViewModel.ViewState.QuestionnaireState -> {
                val state = viewState as QuestionnaireScreenViewModel.ViewState.QuestionnaireState
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                ) {
                    val page = state.questionnaire.pages[state.currentPageIndex]
                    for (question in page.questions) {
                        SliderQuestion(question)
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

    OptionalSlider(
        value = value,
        onValueChange = { value = it },
    )
}
