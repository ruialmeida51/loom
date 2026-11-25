package com.eyther.lumbridge.features.home.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.IconTile
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.home.presentation.model.HomeTaskUi
import com.eyther.lumbridge.features.tasks.presentation.components.TaskDueDate

private val HomeRowMinimumHeight = 60.dp

/** A due task; overdue ones get the error tint so they stand out without reading the date. */
@Composable
internal fun HomeTaskRow(task: HomeTaskUi, onClick: () -> Unit) {
    val overdue = (task.date?.daysFromToday ?: 0) < 0
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .heightIn(min = HomeRowMinimumHeight)
                .padding(horizontal = LoomSpacing.content, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconTile(
            glyph = if (task.date?.time != null) Glyph.BELL else Glyph.TASKS,
            tint = if (overdue) MaterialTheme.colorScheme.error else null,
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                task.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            task.date?.let { TaskDueDate(date = it) }
        }
        LoomIcon(
            glyph = Glyph.CHEVRON_RIGHT,
            tint = MaterialTheme.colorScheme.outline,
            decorative = true,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** The last row of Home's task card: a quiet way in to add one more. */
@Composable
internal fun HomeAddTaskRow(label: String, onClick: () -> Unit) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick)
                .heightIn(min = 52.dp)
                .padding(horizontal = LoomSpacing.content)
                .testTag("home_add_task"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconTile(glyph = Glyph.PLUS)
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
