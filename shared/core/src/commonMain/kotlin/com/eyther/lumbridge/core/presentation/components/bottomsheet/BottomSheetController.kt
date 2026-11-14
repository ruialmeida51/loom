package com.eyther.lumbridge.core.presentation.components.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.eyther.lumbridge.core.presentation.components.bottomsheet.model.BottomSheetType
import kotlin.reflect.KClass

@Stable interface BottomSheetType

/**
 * Each destination owns a controller. Switching a form replaces it instead of stacking modal
 * windows.
 */
@Stable
class BottomSheetController {
    internal val currentSheet = mutableStateOf<BottomSheetType?>(null)
    val registry =
        mutableMapOf<KClass<out BottomSheetType>, @Composable (BottomSheetType) -> Unit>()

    inline fun <reified T : BottomSheetType> registerBottomSheet(
        noinline content: @Composable (T) -> Unit,
    ) {
        registry[T::class] = { sheet -> content(sheet as T) }
    }

    fun showBottomSheet(sheet: BottomSheetType) {
        currentSheet.value = sheet
    }

    fun hideBottomSheet() {
        currentSheet.value = null
    }
}

@Composable
fun rememberBottomSheetController(): BottomSheetController = remember { BottomSheetController() }
