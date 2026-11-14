package com.eyther.lumbridge.app.navigation.model.sheet

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.presentation.components.bottomsheet.model.BottomSheetType

@Stable
internal sealed interface SettingsSheets : BottomSheetType {
    @Stable data object Nickname : SettingsSheets

    @Stable data object Invitation : SettingsSheets
}
