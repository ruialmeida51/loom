package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.AddPill
import com.eyther.lumbridge.core.presentation.components.PillChip
import com.eyther.lumbridge.core.presentation.components.PillRowHeader
import com.eyther.lumbridge.core.presentation.components.bleedHorizontally
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_accounts
import com.eyther.lumbridge.features.expenses.generated.resources.account_header
import com.eyther.lumbridge.features.expenses.generated.resources.account_new_short
import com.eyther.lumbridge.features.expenses.presentation.mapper.accountLabel
import com.eyther.lumbridge.features.expenses.presentation.mapper.glyph
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

/**
 * Money accounts as pills, matching shopping lists: tap another account to switch, tap the current
 * one (marked by its caret) to manage accounts. [menu] holds the screen's other actions.
 */
@Composable
internal fun ExpenseAccountChips(
    accounts: ImmutableList<ExpenseAccountDomain>,
    selected: ExpenseAccountDomain?,
    onSelect: (String) -> Unit,
    onManage: () -> Unit,
    onCreate: () -> Unit,
    menu: List<ActionMenuItem>,
    modifier: Modifier = Modifier,
) {
    val manage = stringResource(Res.string.account_accounts)
    // An archived account being viewed keeps its pill so the selection stays visible.
    val shown = if (selected != null && accounts.none { it.id == selected.id }) accounts + selected else accounts
    Column(modifier = modifier) {
        PillRowHeader(title = stringResource(Res.string.account_header)) {
            ActionMenu(modifier = Modifier.testTag("balances_actions"), items = menu)
        }
        Row(
            modifier =
                Modifier.bleedHorizontally(LoomSpacing.content)
                    .horizontalScroll(rememberScrollState())
                    .padding(
                        start = LoomSpacing.content,
                        end = LoomSpacing.content,
                        top = 2.dp,
                        bottom = 4.dp,
                    )
                    .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            shown.forEach { account ->
                val isSelected = account.id == selected?.id
                val label = accountLabel(account)
                PillChip(
                    text = label,
                    selected = isSelected,
                    leadingGlyph = account.glyph(),
                    trailingGlyph = if (isSelected) Glyph.CHEVRON_DOWN else null,
                    onClick = { if (isSelected) onManage() else onSelect(account.id) },
                    modifier =
                        if (isSelected)
                            Modifier.testTag("expense_accounts").semantics {
                                contentDescription = "$label, $manage"
                            }
                        else Modifier.testTag("account_chip_${account.name}"),
                )
            }
            AddPill(
                label = stringResource(Res.string.account_new_short),
                onClick = onCreate,
                modifier = Modifier.testTag("expense_new_account"),
            )
        }
    }
}
