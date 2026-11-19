package com.eyther.lumbridge.app.navigation.model.sheet

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.presentation.components.bottomsheet.model.BottomSheetType

@Stable
internal sealed interface RecurringExpenseFormSheets : BottomSheetType {
    @Stable data object Date : RecurringExpenseFormSheets

    @Stable data class Split(val amountMinor: Long) : RecurringExpenseFormSheets
}
