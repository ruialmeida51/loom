package com.eyther.lumbridge.features.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.eyther.lumbridge.features.home.generated.resources.Res
import com.eyther.lumbridge.features.home.generated.resources.home_add_expense
import com.eyther.lumbridge.features.home.generated.resources.home_add_reminder
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HomeQuickActions(onAddExpense: () -> Unit, onAddReminder: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickActionTile(
            title = stringResource(Res.string.home_add_expense),
            glyph = Glyph.MONEY,
            onClick = onAddExpense,
            modifier = Modifier.weight(1f).testTag("home_add_expense"),
        )
        QuickActionTile(
            title = stringResource(Res.string.home_add_reminder),
            glyph = Glyph.BELL,
            onClick = onAddReminder,
            modifier = Modifier.weight(1f).testTag("home_add_reminder"),
        )
    }
}

@Composable
private fun QuickActionTile(title: String, glyph: Glyph, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoomIcon(
                glyph = glyph,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}
