package com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.ui_cancel
import com.eyther.lumbridge.features.shopping.generated.resources.ui_list_name
import com.eyther.lumbridge.features.shopping.generated.resources.ui_name_your_list
import com.eyther.lumbridge.features.shopping.generated.resources.ui_save_list
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingListNameBottomSheet(
    listName: String,
    busy: Boolean,
    formError: TextResource?,
    onCloseListForm: () -> Unit,
    onListName: (String) -> Unit,
    onSaveList: () -> Unit,
    onDismiss: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.ui_name_your_list),
        action = {
            LoomButton(stringResource(Res.string.ui_save_list), onSaveList, loading = busy)
        },
        dismissAction = {
            TextButton(onClick = onCloseListForm, enabled = !busy) {
                Text(stringResource(Res.string.ui_cancel))
            }
        },
    ) {
        Column {
            OutlinedTextField(
                listName,
                onListName,
                label = { Text(stringResource(Res.string.ui_list_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy,
            )
            formError?.let { message ->
                ErrorNotice(message = message, onDismiss = onDismiss)
            }
        }
    }
}
