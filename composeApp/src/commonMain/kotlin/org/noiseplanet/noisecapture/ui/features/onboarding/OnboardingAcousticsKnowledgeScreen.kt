package org.noiseplanet.noisecapture.ui.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.acoustics_knowledge_beginner_description
import noisecapture.composeapp.generated.resources.acoustics_knowledge_beginner_title
import noisecapture.composeapp.generated.resources.acoustics_knowledge_confirmed_description
import noisecapture.composeapp.generated.resources.acoustics_knowledge_confirmed_title
import noisecapture.composeapp.generated.resources.acoustics_knowledge_expert_description
import noisecapture.composeapp.generated.resources.acoustics_knowledge_expert_title
import noisecapture.composeapp.generated.resources.check
import noisecapture.composeapp.generated.resources.onboarding_acoustics_knowledge_body
import noisecapture.composeapp.generated.resources.onboarding_acoustics_knowledge_title
import noisecapture.composeapp.generated.resources.onboarding_continue
import noisecapture.composeapp.generated.resources.onboarding_illustration_knowledge
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.noiseplanet.noisecapture.model.enums.AcousticsKnowledgeLevel
import org.noiseplanet.noisecapture.ui.navigation.router.OnboardingRouter
import org.noiseplanet.noisecapture.ui.theme.NoiseLevelColorRamp
import org.noiseplanet.noisecapture.ui.theme.defaultMarkdownTypography


@Composable
fun OnboardingAcousticsKnowledgeScreen(
    viewModel: OnboardingScreenViewModel,
    router: OnboardingRouter,
    modifier: Modifier = Modifier,
) {

    // - Properties

    val items: List<AcousticsKnowledgeItem> = listOf(
        AcousticsKnowledgeItem(
            title = Res.string.acoustics_knowledge_beginner_title,
            description = Res.string.acoustics_knowledge_beginner_description,
            level = AcousticsKnowledgeLevel.BEGINNER,
        ),
        AcousticsKnowledgeItem(
            title = Res.string.acoustics_knowledge_confirmed_title,
            description = Res.string.acoustics_knowledge_confirmed_description,
            level = AcousticsKnowledgeLevel.CONFIRMED,
        ),
        AcousticsKnowledgeItem(
            title = Res.string.acoustics_knowledge_expert_title,
            description = Res.string.acoustics_knowledge_expert_description,
            level = AcousticsKnowledgeLevel.EXPERT,
        ),
    )
    val currentAcousticsKnowledgeLevel by viewModel.acousticsKnowledgeLevel.collectAsStateWithLifecycle()
    val selectedItemIndex: Int = items.indexOfFirst { it.level == currentAcousticsKnowledgeLevel }


    // - Layout

    OnboardingScreenContainer(
        primaryButtonTitle = Res.string.onboarding_continue,
        primaryButtonAction = { router.goToNextStep() },
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(Res.drawable.onboarding_illustration_knowledge),
            contentScale = ContentScale.Inside,
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(Res.string.onboarding_acoustics_knowledge_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Markdown(
            content = stringResource(Res.string.onboarding_acoustics_knowledge_body).trimIndent(),
            typography = defaultMarkdownTypography(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.border(
                width = 1.dp,
                color = NoiseLevelColorRamp.level1Light,
                shape = MaterialTheme.shapes.large
            ).clip(shape = MaterialTheme.shapes.large)
        ) {
            items.forEachIndexed { index, item ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.setAcousticsKnowledgeLevel(item.level) }
                        .background(
                            color = if (selectedItemIndex == index) {
                                NoiseLevelColorRamp.level4Light
                            } else {
                                Color.Transparent
                            }
                        )
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(item.title),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        )
                        Text(
                            text = stringResource(item.description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Icon(
                        painter = painterResource(Res.drawable.check),
                        contentDescription = "check",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(24.dp)
                            .background(
                                color = if (selectedItemIndex == index) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    Color.Transparent
                                },
                                shape = CircleShape,
                            )
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            )
                            .padding(2.dp),
                    )
                }

                if (index < items.size - 1) {
                    HorizontalDivider(color = NoiseLevelColorRamp.level1Light)
                }
            }
        }
    }
}


private data class AcousticsKnowledgeItem(
    val title: StringResource,
    val description: StringResource,
    val level: AcousticsKnowledgeLevel,
)
