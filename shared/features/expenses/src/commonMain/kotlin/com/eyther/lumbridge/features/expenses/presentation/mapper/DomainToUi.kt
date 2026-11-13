package com.eyther.lumbridge.features.expenses.presentation.mapper

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.token_amounts
import com.eyther.lumbridge.features.expenses.generated.resources.token_bills
import com.eyther.lumbridge.features.expenses.generated.resources.token_equal
import com.eyther.lumbridge.features.expenses.generated.resources.token_food_drink
import com.eyther.lumbridge.features.expenses.generated.resources.token_groceries
import com.eyther.lumbridge.features.expenses.generated.resources.token_home
import com.eyther.lumbridge.features.expenses.generated.resources.token_other
import com.eyther.lumbridge.features.expenses.generated.resources.token_percent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun expenseOptionLabel(value: String): String =
    when (value) {
        "Groceries" -> stringResource(Res.string.token_groceries)
        "Bills" -> stringResource(Res.string.token_bills)
        "Food & drink" -> stringResource(Res.string.token_food_drink)
        "Home" -> stringResource(Res.string.token_home)
        "Other" -> stringResource(Res.string.token_other)
        "Equal" -> stringResource(Res.string.token_equal)
        "Amounts" -> stringResource(Res.string.token_amounts)
        "Percent" -> stringResource(Res.string.token_percent)
        else -> value
    }

@Composable fun expenseCategoryLabel(value: String): String = expenseOptionLabel(value)
