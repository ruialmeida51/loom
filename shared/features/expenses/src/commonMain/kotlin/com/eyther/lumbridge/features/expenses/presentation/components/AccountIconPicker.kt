package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.core.presentation.theme.LoomSquircle
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountIconDomain
import com.eyther.lumbridge.features.expenses.presentation.mapper.toGlyph

private val IconChoiceSize = 48.dp // A full touch target per choice.

/** One choice per account icon, like the shopping list icons; each icon's label names it. */
@Composable
internal fun AccountIconPicker(
    selected: ExpenseAccountIconDomain,
    enabled: Boolean,
    onSelected: (ExpenseAccountIconDomain) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
    ) {
        ExpenseAccountIconDomain.entries.forEach { icon ->
            val isSelected = icon == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier.size(IconChoiceSize)
                        .clip(LoomSquircle)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainer,
                        )
                        .selectable(
                            selected = isSelected,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = { onSelected(icon) },
                        )
                        .testTag("account_icon_${icon.key}"),
            ) {
                LoomIcon(
                    glyph = icon.toGlyph(),
                    tint =
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
