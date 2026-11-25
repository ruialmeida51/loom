package com.eyther.lumbridge.features.shopping.presentation.components

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
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListIconDomain
import com.eyther.lumbridge.features.shopping.presentation.mapper.toGlyph

private val IconChoiceSize = 48.dp // A full touch target per choice.

/** One choice per icon; the icon's own label names it for screen readers. */
@Composable
fun ShoppingListIconPicker(
    selected: ShoppingListIconDomain,
    enabled: Boolean,
    onSelected: (ShoppingListIconDomain) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
    ) {
        ShoppingListIconDomain.entries.forEach { icon ->
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
                        .testTag("list_icon_${icon.key}"),
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
