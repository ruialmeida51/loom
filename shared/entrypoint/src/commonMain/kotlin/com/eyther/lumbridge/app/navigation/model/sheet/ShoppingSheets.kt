package com.eyther.lumbridge.app.navigation.model.sheet

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.presentation.components.bottomsheet.model.BottomSheetType

@Stable
internal sealed interface ShoppingSheets : BottomSheetType {
    @Stable data object ShoppingItem : ShoppingSheets

    @Stable data object ShoppingLists : ShoppingSheets

    @Stable data object ShoppingListName : ShoppingSheets

    @Stable data object ShoppingFavorites : ShoppingSheets
}
