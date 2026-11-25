package com.eyther.lumbridge.features.household.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.household_current
import org.jetbrains.compose.resources.stringResource

/**
 * One household in the chooser, after Plannot's tenant cards: a large badge, the name and its
 * details, with the current household highlighted and checked.
 */
@Composable
fun HouseholdChoiceCard(
    household: HouseholdDomain,
    current: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color =
            if (current) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor =
            if (current) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
        shadowElevation = if (!current && LoomColors.cardShadow) 1.dp else 0.dp,
        border =
            if (current) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            else BorderStroke(1.dp, LoomColors.cardBorder),
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(min = 76.dp)
                .semantics { selected = current }
                .testTag("household_choice_${household.name}"),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HouseholdBadge(
                id = household.id,
                name = household.name,
                size = 48.dp,
                picturePath = household.imagePath,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    household.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (current) "${stringResource(Res.string.household_current)} · ${household.currency}"
                    else household.currency,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (current) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (current)
                LoomIcon(
                    glyph = Glyph.CHECK,
                    tint = MaterialTheme.colorScheme.primary,
                    decorative = true,
                    modifier = Modifier.size(22.dp),
                )
            else
                LoomIcon(
                    glyph = Glyph.CHEVRON_RIGHT,
                    tint = MaterialTheme.colorScheme.outline,
                    decorative = true,
                    modifier = Modifier.size(18.dp),
                )
        }
    }
}
