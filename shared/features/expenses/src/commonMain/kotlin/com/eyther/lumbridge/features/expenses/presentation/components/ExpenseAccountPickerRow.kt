package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.form_account
import com.eyther.lumbridge.features.expenses.generated.resources.form_account_fixed
import com.eyther.lumbridge.features.expenses.presentation.mapper.accountLabel
import org.jetbrains.compose.resources.stringResource

/**
 * Names the account an expense goes to, first in the form, so an expense started from Home never
 * lands somewhere unexpected. When [canChoose], tapping it lists the other accounts.
 */
@Composable
internal fun ExpenseAccountPickerRow(
    account: ExpenseAccountDomain,
    accounts: List<ExpenseAccountDomain>,
    canChoose: Boolean,
    editing: Boolean,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    NavigationRow(
        title = accountLabel(account),
        description =
            stringResource(if (editing) Res.string.form_account_fixed else Res.string.form_account),
        glyph = if (account.isDefault) Glyph.HOME else Glyph.PLANE,
        onClick = { expanded = true },
        enabled = canChoose,
        modifier = modifier.testTag("expense_account_picker"),
        trailingContent =
            if (!canChoose) ({})
            else {
                {
                    Box {
                        LoomIcon(glyph = Glyph.CHEVRON_DOWN, decorative = true)
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            accounts.forEach { choice ->
                                DropdownMenuItem(
                                    modifier =
                                        Modifier.testTag("expense_account_${choice.name}")
                                            .semantics { selected = choice.id == account.id },
                                    leadingIcon = {
                                        LoomIcon(
                                            glyph =
                                                if (choice.isDefault) Glyph.HOME else Glyph.PLANE,
                                            decorative = true,
                                        )
                                    },
                                    text = { Text(accountLabel(choice)) },
                                    trailingIcon = {
                                        if (choice.id == account.id)
                                            LoomIcon(glyph = Glyph.CHECK, decorative = true)
                                    },
                                    onClick = {
                                        expanded = false
                                        onSelected(choice.id)
                                    },
                                )
                            }
                        }
                    }
                }
            },
    )
}
