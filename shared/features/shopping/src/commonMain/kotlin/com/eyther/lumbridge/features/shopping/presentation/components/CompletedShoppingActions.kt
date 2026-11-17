package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.DestructiveButton
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.completed_delete_selected
import com.eyther.lumbridge.features.shopping.generated.resources.completed_deselect_all
import com.eyther.lumbridge.features.shopping.generated.resources.completed_select_all
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CompletedShoppingActions(
    selectedCount: Int,
    allSelected: Boolean,
    busy: Boolean,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = onSelectAll,
            enabled = !busy,
            modifier = Modifier.testTag("select_all_completed"),
        ) {
            Text(
                stringResource(
                    if (allSelected) Res.string.completed_deselect_all
                    else Res.string.completed_select_all,
                ),
            )
        }
        DestructiveButton(
            text = stringResource(Res.string.completed_delete_selected, selectedCount),
            onClick = onDelete,
            enabled = selectedCount > 0,
            loading = busy,
            modifier = Modifier.testTag("delete_completed"),
        )
    }
}
