package org.noiseplanet.noisecapture.model.enums

import kotlinx.serialization.Serializable
import noisecapture.composeapp.generated.resources.Res
import noisecapture.composeapp.generated.resources.acoustics_knowledge_beginner_description
import noisecapture.composeapp.generated.resources.acoustics_knowledge_beginner_title
import noisecapture.composeapp.generated.resources.acoustics_knowledge_confirmed_description
import noisecapture.composeapp.generated.resources.acoustics_knowledge_confirmed_title
import noisecapture.composeapp.generated.resources.acoustics_knowledge_expert_description
import noisecapture.composeapp.generated.resources.acoustics_knowledge_expert_title
import org.jetbrains.compose.resources.StringResource
import org.noiseplanet.noisecapture.util.IterableEnum
import org.noiseplanet.noisecapture.util.ShortNameRepresentable
import kotlin.enums.EnumEntries

@Serializable
enum class AcousticsKnowledgeLevel : IterableEnum<AcousticsKnowledgeLevel>, ShortNameRepresentable {

    BEGINNER {

        override val fullName: StringResource = Res.string.acoustics_knowledge_beginner_description
        override val shortName: StringResource = Res.string.acoustics_knowledge_beginner_title
    },

    CONFIRMED {

        override val fullName: StringResource = Res.string.acoustics_knowledge_confirmed_description
        override val shortName: StringResource = Res.string.acoustics_knowledge_confirmed_title
    },

    EXPERT {

        override val fullName: StringResource = Res.string.acoustics_knowledge_expert_description
        override val shortName: StringResource = Res.string.acoustics_knowledge_expert_title
    };

    override fun entries(): EnumEntries<AcousticsKnowledgeLevel> = entries
}
