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
import com.eyther.lumbridge.features.home.presentation.model.HomeTaskUi
import com.eyther.lumbridge.features.tasks.presentation.components.TaskDueDate

@Composable
internal fun HomeTaskRow(task: HomeTaskUi, onClick: () -> Unit) {
    Row(
        modifier =
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(LoomSpacing.content),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
        ) {
            Text(task.title, style = MaterialTheme.typography.titleMedium)
            task.date?.let { TaskDueDate(date = it) }
        }
        LoomIcon(glyph = Glyph.ARROW, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
