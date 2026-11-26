package com.eyther.lumbridge.features.expenses.presentation.mapper

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.split_amounts
import com.eyther.lumbridge.features.expenses.generated.resources.split_equal
import com.eyther.lumbridge.features.expenses.generated.resources.split_percentages
import com.eyther.lumbridge.features.expenses.generated.resources.split_shares
import com.eyther.lumbridge.features.expenses.generated.resources.split_tab_amounts
import com.eyther.lumbridge.features.expenses.generated.resources.split_tab_equal
import com.eyther.lumbridge.features.expenses.generated.resources.split_tab_percentages
import com.eyther.lumbridge.features.expenses.generated.resources.split_tab_shares
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

/** The short label for the split sheet's tabs, where four methods share one row. */
@Composable
internal fun splitMethodTabLabel(method: ExpenseSplitMethodDomain): String =
    stringResource(
        when (method) {
            ExpenseSplitMethodDomain.EQUAL -> Res.string.split_tab_equal
            ExpenseSplitMethodDomain.SHARES -> Res.string.split_tab_shares
            ExpenseSplitMethodDomain.AMOUNTS -> Res.string.split_tab_amounts
            ExpenseSplitMethodDomain.PERCENTAGES -> Res.string.split_tab_percentages
        },
    )
