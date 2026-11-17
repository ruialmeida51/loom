package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.generated.resources.Res
import com.eyther.lumbridge.core.generated.resources.ui_dismiss
import com.eyther.lumbridge.core.presentation.resources.TextResource
import org.jetbrains.compose.resources.stringResource

/** A visible error. Omit dismissal for initial read failures that still need an explicit retry. */
@Composable
fun ErrorNotice(message: TextResource, onDismiss: (() -> Unit)? = null) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                message.getString(),
                Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
            onDismiss?.let { dismiss ->
                OutlinedButton(onClick = dismiss) { Text(stringResource(Res.string.ui_dismiss)) }
            }
        }
    }
}
