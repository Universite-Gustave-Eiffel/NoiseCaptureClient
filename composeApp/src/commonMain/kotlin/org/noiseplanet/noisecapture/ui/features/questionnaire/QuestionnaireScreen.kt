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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.previous
import org.noiseplanet.noisecapture.model.dao.Question
import org.noiseplanet.noisecapture.model.dao.QuestionAnswer
import org.noiseplanet.noisecapture.model.dao.QuestionnaireAnswers
import org.noiseplanet.noisecapture.ui.components.ButtonContent
import org.noiseplanet.noisecapture.ui.components.Container
import org.noiseplanet.noisecapture.ui.components.NCButton
import org.noiseplanet.noisecapture.ui.components.OptionalSlider
import org.noiseplanet.noisecapture.ui.components.tertiaryContainerColors
import org.noiseplanet.noisecapture.ui.navigation.router.QuestionnaireRouter
import org.noiseplanet.noisecapture.ui.theme.Noise
import org.noiseplanet.noisecapture.ui.theme.titleMono

@Composable
fun QuestionnaireScreen(
    viewModel: QuestionnaireScreenViewModel,
    router: QuestionnaireRouter,
) {
    // - Properties

    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val viewState: QuestionnaireScreenViewModel.ViewState? by viewModel.viewState.collectAsStateWithLifecycle()
    val answers = remember { mutableStateMapOf<String, QuestionAnswer>() }


    // - Lifecycle

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.onQuestionnaireDone = router::goToMeasurementDetails
                }

                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.onQuestionnaireDone = null
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    // - Layout

    Surface {
        // Prevent back gestures in questionnaire
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = true,
            onBackCompleted = {
                if (viewModel.confirmPopBackStack()) {
                    router.popBackStack()
                }
            }
        )

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
                        SliderQuestion(
                            question = question,
                            value = answers[question.id]?.answer,
                            onValueChange = {
                                answers[question.id] = QuestionAnswer(question.id, it)
                            }
                        )
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
                            onClick = {
                                viewModel.confirmPage(
                                    answers = QuestionnaireAnswers(
                                        questionnaireId = state.questionnaire.id,
                                        answers = answers.values.toList()
                                    )
                                )
                            }
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
    value: Float?,
    onValueChange: (Float) -> Unit,
) {
    // - Layout

    Column {
        Text(text = question.title, style = MaterialTheme.typography.titleSmall)
        question.description?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }

        OptionalSlider(
            value = value,
            onValueChange = onValueChange,
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
