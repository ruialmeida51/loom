package com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_accounts
import com.eyther.lumbridge.features.expenses.generated.resources.account_manage
import com.eyther.lumbridge.features.expenses.generated.resources.account_new
import com.eyther.lumbridge.features.expenses.generated.resources.account_selected
import com.eyther.lumbridge.features.expenses.presentation.mapper.accountLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseAccountsBottomSheet(
    active: List<ExpenseAccountDomain>,
    selectedId: String?,
    busy: Boolean,
    onSelect: (String) -> Unit,
    onCreate: () -> Unit,
    onManage: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.account_accounts),
        action = {
            LoomButton(
                text = stringResource(Res.string.account_new),
                onClick = onCreate,
                enabled = !busy,
            )
        },
        dismissAction = {},
    ) {
        GroupedSurface {
            active.forEachIndexed { index, account ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                NavigationRow(
                    title = accountLabel(account),
                    glyph = Glyph.MONEY,
                    description = account.currency,
                    value =
                        if (account.id == selectedId) stringResource(Res.string.account_selected)
                        else null,
                    trailingGlyph = if (account.id == selectedId) Glyph.CHECK else Glyph.ARROW,
                    onClick = { onSelect(account.id) },
                    enabled = !busy,
                )
            }
        }
        GroupedSurface {
            NavigationRow(
                title = stringResource(Res.string.account_manage),
                glyph = Glyph.SETTINGS,
                onClick = onManage,
                enabled = !busy,
            )
        }
    }
}
