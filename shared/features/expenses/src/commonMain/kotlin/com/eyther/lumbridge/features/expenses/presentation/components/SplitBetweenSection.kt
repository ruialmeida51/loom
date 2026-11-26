package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.model.isGuest
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.guest_tag
import com.eyther.lumbridge.features.expenses.generated.resources.split_each
import com.eyther.lumbridge.features.expenses.generated.resources.split_equally_between
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import org.jetbrains.compose.resources.stringResource

/**
 * The common split, right on the form: everyone selected pays the same, and tapping a person adds
 * or removes them. Uneven splits live in the split sheet.
 */
@Composable
internal fun SplitBetweenSection(
    members: List<MemberDomain>,
    selected: Set<String>,
    share: String?,
    enabled: Boolean,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = LoomSpacing.content, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.split_equally_between),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            share?.let {
                Text(
                    stringResource(Res.string.split_each, it),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("split_each"),
                )
            }
        }
        val guest = stringResource(Res.string.guest_tag)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
        ) {
            members.forEach { member ->
                val isIn = member.id in selected
                FilterChip(
                    selected = isIn,
                    onClick = { onToggle(member.id) },
                    enabled = enabled,
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    // People left out fade, so who shares the cost reads at a glance.
                    leadingIcon = {
                        MemberAvatar(
                            memberId = member.id,
                            initials = member.name.trim().take(1).uppercase(),
                            picturePath = member.avatarPath,
                            modifier = Modifier.size(20.dp).alpha(if (isIn) 1f else 0.4f),
                        )
                    },
                    trailingIcon =
                        if (isIn) {
                            {
                                LoomIcon(
                                    glyph = Glyph.CHECK,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    decorative = true,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        } else null,
                    label = {
                        Text(if (member.isGuest) "${member.name} · $guest" else member.name)
                    },
                    modifier = Modifier.testTag("split_person_${member.name}"),
                )
            }
        }
    }
}
