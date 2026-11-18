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
import com.eyther.lumbridge.features.expenses.generated.resources.account_archive
import com.eyther.lumbridge.features.expenses.generated.resources.account_archived
import com.eyther.lumbridge.features.expenses.generated.resources.account_delete
import com.eyther.lumbridge.features.expenses.generated.resources.account_manage
import com.eyther.lumbridge.features.expenses.generated.resources.account_manage_hint
import com.eyther.lumbridge.features.expenses.generated.resources.account_rename
import com.eyther.lumbridge.features.expenses.generated.resources.account_restore
import com.eyther.lumbridge.features.expenses.generated.resources.ui_close
import com.eyther.lumbridge.features.expenses.presentation.mapper.accountLabel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.stringResource

@Composable
fun ManageExpenseAccountsBottomSheet(
    accounts: ImmutableList<ExpenseAccountDomain>,
    manageableAccountIds: ImmutableSet<String>,
    busy: Boolean,
    error: TextResource?,
    onSelect: (String) -> Unit,
    onEdit: (ExpenseAccountDomain) -> Unit,
    onArchive: (ExpenseAccountDomain) -> Unit,
    onDelete: (ExpenseAccountDomain) -> Unit,
    onClose: () -> Unit,
    onDismissError: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.account_manage),
        onAction = onClose,
        actionLabel = stringResource(Res.string.ui_close),
        busy = busy,
    ) {
        Text(
            stringResource(Res.string.account_manage_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GroupedSurface {
            accounts.forEachIndexed { index, account ->
                key(account.id) {
                    if (index > 0)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NavigationRow(
                            title = accountLabel(account),
                            glyph = Glyph.MONEY,
                            description =
                                if (account.archived) stringResource(Res.string.account_archived)
                                else account.currency,
                            onClick = { onSelect(account.id) },
                            enabled = !busy,
                            modifier = Modifier.weight(1f).testTag("manage_account_${account.id}"),
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
        error?.let { ErrorNotice(message = it, onDismiss = onDismissError) }
    }
}
