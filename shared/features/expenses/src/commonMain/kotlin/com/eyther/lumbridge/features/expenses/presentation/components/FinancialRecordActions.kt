package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete
import com.eyther.lumbridge.features.expenses.generated.resources.history_edit

/**
 * The details toolbar: Share first (when the record can be shared), then the ⋮ menu with Edit and
 * Delete, the same order as the Share action in list headers. The owning Host supplies
 * navigation; the feature retains permission and confirmation state.
 */
@Composable
fun FinancialRecordActions(
    testTag: String,
    canEdit: Boolean,
    busy: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: (() -> Unit)? = null,
) {
    onShare?.let { share ->
        IconButton(onClick = share, modifier = Modifier.testTag("${testTag}_share")) {
            LoomIcon(glyph = Glyph.SHARE)
        }
    }
    if (canEdit)
        ActionMenu(
            modifier = Modifier.testTag(testTag),
            items =
                listOf(
                    ActionMenuItem(
                        text = TextResource.Resource(Res.string.history_edit),
                        onClick = onEdit,
                        enabled = !busy,
                    ),
                    ActionMenuItem(
                        text = TextResource.Resource(Res.string.detail_delete),
                        onClick = onDelete,
                        enabled = !busy,
                        destructive = true,
                    ),
                ),
        )
}
