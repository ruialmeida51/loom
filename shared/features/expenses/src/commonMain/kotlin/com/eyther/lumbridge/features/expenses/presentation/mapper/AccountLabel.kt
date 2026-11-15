package com.eyther.lumbridge.features.expenses.presentation.mapper

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_everyday
import org.jetbrains.compose.resources.stringResource

@Composable
fun accountLabel(account: ExpenseAccountDomain): String =
    if (account.isDefault) stringResource(Res.string.account_everyday) else account.name
