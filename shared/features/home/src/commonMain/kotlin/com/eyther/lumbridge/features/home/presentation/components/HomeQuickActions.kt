package com.eyther.lumbridge.features.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.home.generated.resources.Res
import com.eyther.lumbridge.features.home.generated.resources.home_add_expense
import com.eyther.lumbridge.features.home.generated.resources.home_add_task
import org.jetbrains.compose.resources.stringResource

private val QuickActionIconSize = 24.dp // Standard icon size inside the square action area.
private val MinimumQuickActionHeight =
    88.dp // Keep a square icon and readable label inside a compact action.

@Composable
internal fun HomeQuickActions(onAddExpense: () -> Unit, onAddTask: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium)) {
        QuickActionTile(
            title = stringResource(Res.string.home_add_expense),
            glyph = Glyph.MONEY,
            onClick = onAddExpense,
            modifier = Modifier.weight(1f).testTag("home_add_expense"),
        )
        QuickActionTile(
            title = stringResource(Res.string.home_add_task),
            glyph = Glyph.TASKS,
            onClick = onAddTask,
            modifier = Modifier.weight(1f).testTag("home_add_task"),
        )
    }
}

@Composable
private fun QuickActionTile(title: String, glyph: Glyph, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = MinimumQuickActionHeight),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(LoomSpacing.medium),
            horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.testTag("home_action_icon_$glyph"),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                LoomIcon(
                    glyph = glyph,
                    modifier = Modifier.padding(LoomSpacing.medium).size(QuickActionIconSize),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
