package com.eyther.lumbridge.features.home.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.features.home.generated.resources.Res
import com.eyther.lumbridge.features.home.generated.resources.home_overdue
import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HomeReminderRow(reminder: HomeReminderUi, onClick: () -> Unit) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(onClick = onClick)
                .heightIn(min = 56.dp)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(reminder.title, style = MaterialTheme.typography.bodyLarge)
            Text(
                if (reminder.overdue) stringResource(Res.string.home_overdue, reminder.due)
                else reminder.due,
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (reminder.overdue) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LoomIcon(glyph = Glyph.ARROW, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
