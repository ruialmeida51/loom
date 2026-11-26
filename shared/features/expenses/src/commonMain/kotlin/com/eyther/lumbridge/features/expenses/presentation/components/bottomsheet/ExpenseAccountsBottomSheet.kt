package com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_accounts
import com.eyther.lumbridge.features.expenses.generated.resources.account_archive
import com.eyther.lumbridge.features.expenses.generated.resources.account_archived
import com.eyther.lumbridge.features.expenses.generated.resources.account_delete
import com.eyther.lumbridge.features.expenses.generated.resources.account_new
import com.eyther.lumbridge.features.expenses.generated.resources.account_rename
import com.eyther.lumbridge.features.expenses.generated.resources.account_restore
import com.eyther.lumbridge.features.expenses.presentation.mapper.accountLabel
import com.eyther.lumbridge.features.expenses.presentation.mapper.glyph
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.stringResource

/** Select immediately; manage each account in place, as in the shopping-list selector. */
@Composable
fun ExpenseAccountsBottomSheet(
    active: ImmutableList<ExpenseAccountDomain>,
    archived: ImmutableList<ExpenseAccountDomain>,
    manageableAccountIds: ImmutableSet<String>,
    selectedId: String?,
    busy: Boolean,
    error: TextResource?,
    onSelect: (String) -> Unit,
    onCreate: () -> Unit,
    onEdit: (ExpenseAccountDomain) -> Unit,
    onArchive: (ExpenseAccountDomain) -> Unit,
    onDelete: (ExpenseAccountDomain) -> Unit,
    onDismissError: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.account_accounts),
        onAction = onCreate,
        actionLabel = stringResource(Res.string.account_new),
        busy = busy,
    ) {
        AccountSelectionGroup(
            accounts = active,
            manageableAccountIds = manageableAccountIds,
            selectedId = selectedId,
            busy = busy,
            onSelect = onSelect,
            onEdit = onEdit,
            onArchive = onArchive,
            onDelete = onDelete,
        )
        if (archived.isNotEmpty()) {
            Text(
                stringResource(Res.string.account_archived),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            AccountSelectionGroup(
                accounts = archived,
                manageableAccountIds = manageableAccountIds,
                selectedId = selectedId,
                busy = busy,
                onSelect = onSelect,
                onEdit = onEdit,
                onArchive = onArchive,
                onDelete = onDelete,
            )
        }
        error?.let { ErrorNotice(message = it, onDismiss = onDismissError) }
    }
}

@Composable
private fun AccountSelectionGroup(
    accounts: ImmutableList<ExpenseAccountDomain>,
    manageableAccountIds: ImmutableSet<String>,
    selectedId: String?,
    busy: Boolean,
    onSelect: (String) -> Unit,
    onEdit: (ExpenseAccountDomain) -> Unit,
    onArchive: (ExpenseAccountDomain) -> Unit,
    onDelete: (ExpenseAccountDomain) -> Unit,
) {
    GroupedSurface {
        accounts.forEachIndexed { index, account ->
            key(account.id) {
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NavigationRow(
                        title = accountLabel(account),
                        glyph = account.glyph(),
                        description = account.currency,
                        trailingGlyph = if (account.id == selectedId) Glyph.CHECK else Glyph.CHEVRON_RIGHT,
                        onClick = { onSelect(account.id) },
                        enabled = !busy && !account.archived,
                        modifier =
                            Modifier.weight(1f).testTag("manage_account_${account.id}").semantics {
                                selected = account.id == selectedId
                            },
                    )
                    if (account.id in manageableAccountIds)
                        ActionMenu(
                            modifier = Modifier.testTag("account_actions_${account.name}"),
                            items =
                                listOf(
                                    ActionMenuItem(
                                        text = TextResource.Resource(Res.string.account_rename),
                                        onClick = { onEdit(account) },
                                        enabled = !busy,
                                    ),
                                    ActionMenuItem(
                                        text =
                                            TextResource.Resource(
                                                if (account.archived) Res.string.account_restore
                                                else Res.string.account_archive,
                                            ),
                                        onClick = { onArchive(account) },
                                        enabled = !busy,
                                    ),
                                    ActionMenuItem(
                                        text = TextResource.Resource(Res.string.account_delete),
                                        onClick = { onDelete(account) },
                                        enabled = !busy,
                                        destructive = true,
                                    ),
                                ),
                        )
                }
            }
        }
    }
}
