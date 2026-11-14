package com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.ui_add_item
import com.eyther.lumbridge.features.shopping.generated.resources.ui_add_to_the_list
import com.eyther.lumbridge.features.shopping.generated.resources.ui_cancel
import com.eyther.lumbridge.features.shopping.generated.resources.ui_category
import com.eyther.lumbridge.features.shopping.generated.resources.ui_edit_item
import com.eyther.lumbridge.features.shopping.generated.resources.ui_one_item_per_line_for_quick_entry
import com.eyther.lumbridge.features.shopping.generated.resources.ui_quantity_or_note
import com.eyther.lumbridge.features.shopping.generated.resources.ui_save_item
import com.eyther.lumbridge.features.shopping.generated.resources.ui_what_do_we_need
import com.eyther.lumbridge.features.shopping.presentation.mapper.shoppingCategoryLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingItemBottomSheet(
    name: String,
    quantity: String,
    category: String,
    editing: Boolean,
    busy: Boolean,
    formError: TextResource?,
    onShowAdd: (Boolean) -> Unit,
    onName: (String) -> Unit,
    onQuantity: (String) -> Unit,
    onCategory: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    FormBottomSheet(
        title =
            if (editing) stringResource(Res.string.ui_edit_item)
            else stringResource(Res.string.ui_add_to_the_list),
        action = {
            LoomButton(
                if (editing) stringResource(Res.string.ui_save_item)
                else stringResource(Res.string.ui_add_item),
                onSave,
                loading = busy,
            )
        },
        dismissAction = {
            TextButton(onClick = { onShowAdd(false) }, enabled = !busy) {
                Text(stringResource(Res.string.ui_cancel))
            }
        },
    ) {
        OutlinedTextField(
            name,
            onName,
            label = { Text(stringResource(Res.string.ui_what_do_we_need)) },
            singleLine = editing,
            maxLines = if (editing) 1 else 5,
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
        )
        if (!editing)
            Text(
                stringResource(Res.string.ui_one_item_per_line_for_quick_entry),
                style = MaterialTheme.typography.bodySmall,
            )
        OutlinedTextField(
            quantity,
            onQuantity,
            label = { Text(stringResource(Res.string.ui_quantity_or_note)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
        )
        Text(
            stringResource(Res.string.ui_category),
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Produce", "Bakery", "Dairy & eggs", "Pantry", "Household").forEach { option ->
                FilterChip(
                    selected = category == option,
                    onClick = { onCategory(option) },
                    label = { Text(shoppingCategoryLabel(option)) },
                    enabled = !busy,
                    modifier = Modifier.testTag("category_$option"),
                )
            }
        }
        formError?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismiss)
        }
    }
}
