package com.eyther.lumbridge.app.navigation.model.sheet

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.presentation.components.bottomsheet.model.BottomSheetType

@Stable
internal sealed interface ExpensesSheets : BottomSheetType {
    @Stable data object SharedExpense : ExpensesSheets

    @Stable data object Settlement : ExpensesSheets
}
