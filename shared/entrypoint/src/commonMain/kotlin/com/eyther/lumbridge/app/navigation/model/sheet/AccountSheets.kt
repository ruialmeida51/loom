package com.eyther.lumbridge.app.navigation.model.sheet

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.presentation.components.bottomsheet.model.BottomSheetType

@Stable
internal sealed interface AccountSheets : BottomSheetType {
    @Stable data object Nickname : AccountSheets

    @Stable data object PaymentDetails : AccountSheets

    @Stable data object UsageData : AccountSheets

    @Stable data object Notifications : AccountSheets
}
