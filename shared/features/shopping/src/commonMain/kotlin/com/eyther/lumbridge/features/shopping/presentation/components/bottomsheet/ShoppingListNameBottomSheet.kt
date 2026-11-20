package com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListIconDomain
import com.eyther.lumbridge.features.shopping.generated.resources.list_icon
import com.eyther.lumbridge.features.shopping.presentation.components.ShoppingListIconPicker
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.ui_list_name
import com.eyther.lumbridge.features.shopping.generated.resources.ui_name_your_list
import org.jetbrains.compose.resources.stringResource

@Composable
fun ShoppingListNameBottomSheet(
    listName: String,
    listIcon: ShoppingListIconDomain,
    busy: Boolean,
    formError: TextResource?,
    onCloseListForm: () -> Unit,
    onListName: (String) -> Unit,
    onListIcon: (ShoppingListIconDomain) -> Unit,
    onSaveList: () -> Unit,
    onDismiss: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.ui_name_your_list),
        onAction = onSaveList,
        onDismiss = onCloseListForm,
        busy = busy,
        actionModifier = Modifier.testTag("save_shopping_list"),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(LoomSpacing.medium)) {
            OutlinedTextField(
                listName,
                onListName,
                label = { Text(stringResource(Res.string.ui_list_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !busy,
            )
            Text(
                stringResource(Res.string.list_icon),
                style = MaterialTheme.typography.titleSmall,
            )
            ShoppingListIconPicker(selected = listIcon, enabled = !busy, onSelected = onListIcon)
            formError?.let { message ->
                ErrorNotice(message = message, onDismiss = onDismiss)
            }
        }
    }
}
