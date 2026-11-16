package com.eyther.lumbridge.features.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi
import com.eyther.lumbridge.features.reminders.presentation.components.ReminderDueDate

@Composable
internal fun HomeReminderRow(reminder: HomeReminderUi, onClick: () -> Unit) {
    LoomCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                ReminderDueDate(date = reminder.date)
            }
            LoomIcon(glyph = Glyph.ARROW, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
