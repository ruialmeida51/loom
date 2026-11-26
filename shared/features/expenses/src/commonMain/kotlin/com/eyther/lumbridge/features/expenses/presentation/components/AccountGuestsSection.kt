package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ActionProgress
import com.eyther.lumbridge.core.presentation.components.DialogActions
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_guest_actions
import com.eyther.lumbridge.features.expenses.generated.resources.account_guest_add
import com.eyther.lumbridge.features.expenses.generated.resources.account_guest_add_title
import com.eyther.lumbridge.features.expenses.generated.resources.account_guest_confirm
import com.eyther.lumbridge.features.expenses.generated.resources.account_guest_limit
import com.eyther.lumbridge.features.expenses.generated.resources.account_guest_name
import com.eyther.lumbridge.features.expenses.generated.resources.account_guest_remove
import com.eyther.lumbridge.features.expenses.generated.resources.account_guest_rename
import com.eyther.lumbridge.features.expenses.generated.resources.account_guest_rename_title
import com.eyther.lumbridge.features.expenses.generated.resources.account_guests
import com.eyther.lumbridge.features.expenses.generated.resources.account_guests_hint
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import org.jetbrains.compose.resources.stringResource

/** The guest being named in the dialog: a new one when [guest] is null. */
private data class GuestNaming(val guest: ExpenseGuestDomain?)

/**
 * The account form's guests: one row each with Rename and Remove, then "Add a guest". Names are
 * asked for in a short dialog; the screen decides whether a change saves at once or with the form.
 */
@Composable
fun AccountGuestsSection(
    guests: List<ExpenseGuestDomain>,
    canAdd: Boolean,
    busyId: String?,
    enabled: Boolean,
    onAdd: (String) -> Unit,
    onRename: (String, String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var naming by remember { mutableStateOf<GuestNaming?>(null) }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(Res.string.account_guests), style = MaterialTheme.typography.titleSmall)
        Text(
            stringResource(Res.string.account_guests_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        guests.forEach { guest ->
            GuestRow(
                guest = guest,
                busy = busyId == guest.id,
                enabled = enabled && busyId == null,
                onRename = { naming = GuestNaming(guest) },
                onRemove = { onRemove(guest.id) },
            )
        }
        LoomSecondaryButton(
            text = stringResource(Res.string.account_guest_add),
            onClick = { naming = GuestNaming(guest = null) },
            enabled = canAdd,
            glyph = Glyph.PLUS,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).testTag("add_account_guest"),
        )
        if (guests.size >= ExpenseGuestDomain.MAXIMUM_PER_ACCOUNT)
            Text(
                stringResource(
                    Res.string.account_guest_limit,
                    ExpenseGuestDomain.MAXIMUM_PER_ACCOUNT,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
    }
    naming?.let { current ->
        GuestNameDialog(
            initial = current.guest?.name.orEmpty(),
            renaming = current.guest != null,
            onDismiss = { naming = null },
            onConfirm = { name ->
                current.guest?.let { onRename(it.id, name) } ?: onAdd(name)
                naming = null
            },
        )
    }
}

@Composable
private fun GuestRow(
    guest: ExpenseGuestDomain,
    busy: Boolean,
    enabled: Boolean,
    onRename: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("guest_${guest.name}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MemberAvatar(memberId = guest.id, initials = guest.name.trim().take(1).uppercase())
        Text(
            guest.name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        GuestTag()
        if (busy) ActionProgress()
        else
            ActionMenu(
                items =
                    listOf(
                        ActionMenuItem(
                            text = TextResource.Resource(Res.string.account_guest_rename),
                            onClick = onRename,
                            enabled = enabled,
                        ),
                        ActionMenuItem(
                            text = TextResource.Resource(Res.string.account_guest_remove),
                            onClick = onRemove,
                            enabled = enabled,
                            destructive = true,
                        ),
                    ),
                contentDescription = stringResource(Res.string.account_guest_actions, guest.name),
                modifier = Modifier.testTag("guest_actions_${guest.name}"),
            )
    }
}

@Composable
private fun GuestNameDialog(
    initial: String,
    renaming: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initial) }
    val valid = ExpenseGuestDomain(id = "", name = name).hasValidName()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (renaming) Res.string.account_guest_rename_title
                    else Res.string.account_guest_add_title,
                ),
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(ExpenseGuestDomain.MAXIMUM_NAME_LENGTH) },
                singleLine = true,
                label = { Text(stringResource(Res.string.account_guest_name)) },
                modifier = Modifier.fillMaxWidth().testTag("guest_name_field"),
            )
        },
        confirmButton = {
            DialogActions(
                confirmText = stringResource(Res.string.account_guest_confirm),
                dismissText = stringResource(Res.string.ui_cancel),
                confirmEnabled = valid,
                confirmModifier = Modifier.testTag("confirm_guest_name"),
                onConfirm = { onConfirm(name) },
                onDismiss = onDismiss,
            )
        },
    )
}
