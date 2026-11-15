package com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_accounts
import com.eyther.lumbridge.features.expenses.generated.resources.account_archived
import com.eyther.lumbridge.features.expenses.generated.resources.account_new
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import com.eyther.lumbridge.features.expenses.presentation.mapper.accountLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseAccountsBottomSheet(
    active: List<ExpenseAccountDomain>,
    archived: List<ExpenseAccountDomain>,
    busy: Boolean,
    onSelect: (String) -> Unit,
    onCreate: () -> Unit,
    onClose: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.account_accounts),
        action = {
            TextButton(onClick = onCreate, enabled = !busy) {
                Text(stringResource(Res.string.account_new))
            }
        },
        dismissAction = {
            TextButton(onClick = onClose) { Text(stringResource(Res.string.ui_cancel)) }
        },
    ) {
        active.forEach { account ->
            NavigationRow(
                title = accountLabel(account),
                description = account.currency,
                onClick = { onSelect(account.id) },
                enabled = !busy,
            )
        }
        if (archived.isNotEmpty()) {
            Text(stringResource(Res.string.account_archived))
            archived.forEach { account ->
                NavigationRow(
                    title = accountLabel(account),
                    description = account.currency,
                    onClick = { onSelect(account.id) },
                    enabled = !busy,
                )
            }
        }
    }
}
