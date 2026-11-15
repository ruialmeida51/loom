package com.eyther.lumbridge.features.expenses.presentation.mapper

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.split_amounts
import com.eyther.lumbridge.features.expenses.generated.resources.split_equal
import com.eyther.lumbridge.features.expenses.generated.resources.split_percentages
import com.eyther.lumbridge.features.expenses.generated.resources.split_shares
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun splitMethodLabel(method: ExpenseSplitMethodDomain): String =
    stringResource(
        when (method) {
            ExpenseSplitMethodDomain.EQUAL -> Res.string.split_equal
            ExpenseSplitMethodDomain.SHARES -> Res.string.split_shares
            ExpenseSplitMethodDomain.AMOUNTS -> Res.string.split_amounts
            ExpenseSplitMethodDomain.PERCENTAGES -> Res.string.split_percentages
        },
    )
