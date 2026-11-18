package com.eyther.lumbridge.app.navigation.model.sheet

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.presentation.components.bottomsheet.model.BottomSheetType

@Stable
internal sealed interface TasksSheets : BottomSheetType {
    @Stable data object Task : TasksSheets

    @Stable data object Notifications : TasksSheets
}
