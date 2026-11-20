package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.DestructiveIconButton
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.entry_discard_description
import com.eyther.lumbridge.features.shopping.generated.resources.entry_retry
import org.jetbrains.compose.resources.stringResource

@Composable
fun PendingShoppingItemRow(
    name: String,
    error: TextResource?,
    indented: Boolean,
    onRetry: () -> Unit,
    onDiscard: () -> Unit,
) {
    Column(Modifier.padding(start = if (indented) ShoppingIndentation else 0.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = false,
                onCheckedChange = null,
                enabled = false,
                modifier = Modifier.size(48.dp),
            )
            Text(name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            if (error == null)
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        }
        error?.let { message ->
            Column(
                modifier = Modifier.padding(start = 48.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(message.getString(), color = MaterialTheme.colorScheme.error)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LoomSecondaryButton(
                        text = stringResource(Res.string.entry_retry),
                        onClick = onRetry,
                        modifier = Modifier.testTag("retry_entry_$name"),
                    )
                    DestructiveIconButton(
                        description = stringResource(Res.string.entry_discard_description, name),
                        onClick = onDiscard,
                    )
                }
            }
        }
    }
}
