package com.eyther.lumbridge.features.expenses.presentation.components.receipt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingRow
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.components.menu.rememberActionMenuState
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_retry
import com.eyther.lumbridge.features.expenses.generated.resources.form_save
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_add
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_camera
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_delete
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_discard
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_gallery
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_options
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_ready
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_title
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_view
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import com.eyther.lumbridge.features.expenses.presentation.model.ReceiptPhotoUi
import org.jetbrains.compose.resources.stringResource

private val DraftPreviewHeight =
    144.dp // A useful preview without turning expense capture into a photo screen.

@Composable
internal fun ExpenseReceiptSection(
    photo: ReceiptPhotoUi?,
    attached: Boolean,
    loading: Boolean,
    preparing: Boolean,
    busy: Boolean,
    canManage: Boolean,
    canDelete: Boolean,
    cameraSupported: Boolean,
    showDraftActions: Boolean,
    error: TextResource?,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onView: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit,
    onInvalidPhoto: () -> Unit,
) {
    val menu = rememberActionMenuState()
    Column(verticalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
        if (loading) GroupedSurface { LoadingRow() }
        else if (canManage || attached || photo != null)
            GroupedSurface {
                if (photo != null)
                    ReceiptImage(
                        photo = photo,
                        modifier =
                            Modifier.fillMaxWidth()
                                .height(DraftPreviewHeight)
                                .padding(LoomSpacing.small),
                        onInvalidPhoto = onInvalidPhoto,
                    )
                NavigationRow(
                    title =
                        stringResource(
                            if (attached || photo != null) Res.string.receipt_title
                            else Res.string.receipt_add,
                        ),
                    description =
                        if (photo != null) stringResource(Res.string.receipt_ready)
                        else if (attached) stringResource(Res.string.receipt_view) else null,
                    glyph = Glyph.PHOTO,
                    onClick = if (attached && photo == null) onView else menu::show,
                    enabled = !busy && (canManage || attached),
                    modifier = Modifier.testTag("expense_receipt"),
                    trailingContent = {
                        if (preparing)
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        else if (canManage)
                            ActionMenu(
                                state = menu,
                                contentDescription = stringResource(Res.string.receipt_options),
                                modifier = Modifier.testTag("receipt_options"),
                                items =
                                    buildList {
                                        add(
                                            ActionMenuItem(
                                                text =
                                                    TextResource.Resource(
                                                        Res.string.receipt_gallery,
                                                    ),
                                                onClick = onGallery,
                                                enabled = !busy,
                                            ),
                                        )
                                        if (cameraSupported)
                                            add(
                                                ActionMenuItem(
                                                    text =
                                                        TextResource.Resource(
                                                            Res.string.receipt_camera,
                                                        ),
                                                    onClick = onCamera,
                                                    enabled = !busy,
                                                ),
                                            )
                                        if (photo != null)
                                            add(
                                                ActionMenuItem(
                                                    text =
                                                        TextResource.Resource(
                                                            Res.string.receipt_discard,
                                                        ),
                                                    onClick = onDiscard,
                                                    enabled = !busy,
                                                ),
                                            )
                                        else if (canDelete && attached)
                                            add(
                                                ActionMenuItem(
                                                    text =
                                                        TextResource.Resource(
                                                            Res.string.receipt_delete,
                                                        ),
                                                    onClick = onDelete,
                                                    enabled = !busy,
                                                    destructive = true,
                                                ),
                                            )
                                    },
                            )
                        else LoomIcon(glyph = Glyph.ARROW, decorative = true)
                    },
                )
            }
        error?.let { message ->
            ErrorNotice(message = message)
            if (!canManage && !attached && photo == null)
                LoomSecondaryButton(
                    text = stringResource(Res.string.balance_retry),
                    onClick = onRetry,
                    enabled = !busy,
                )
        }
        if (photo != null && showDraftActions)
            Row(horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
                LoomSecondaryButton(
                    text = stringResource(Res.string.ui_cancel),
                    onClick = onDiscard,
                    enabled = !busy,
                    modifier = Modifier.weight(1f),
                )
                LoomButton(
                    text = stringResource(Res.string.form_save),
                    onClick = onSave,
                    loading = busy,
                    modifier = Modifier.weight(1f).testTag("save_receipt"),
                )
            }
    }
}
