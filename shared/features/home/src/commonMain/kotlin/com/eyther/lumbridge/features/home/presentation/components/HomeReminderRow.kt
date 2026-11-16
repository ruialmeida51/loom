package com.eyther.lumbridge.features.home.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi
import com.eyther.lumbridge.features.reminders.presentation.components.ReminderDueDate

@Composable
internal fun HomeReminderRow(reminder: HomeReminderUi, onClick: () -> Unit) {
    Row(
        modifier =
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(LoomSpacing.content),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
        ) {
            Text(reminder.title, style = MaterialTheme.typography.titleMedium)
            ReminderDueDate(date = reminder.date)
        }
        LoomIcon(glyph = Glyph.ARROW, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
