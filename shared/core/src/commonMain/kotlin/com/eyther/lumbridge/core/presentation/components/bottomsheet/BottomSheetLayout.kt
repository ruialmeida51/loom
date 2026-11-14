package com.eyther.lumbridge.core.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.bottomsheet.model.BottomSheetType

/** Material owns sheet/IME insets here; feature content only supplies fields and actions. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetLayout(
    controller: BottomSheetController,
    dismissEnabled: Boolean = true,
    onDismissed: () -> Unit,
    content: @Composable () -> Unit,
) {
    val requestedSheet by controller.currentSheet
    var displayedSheet by remember { mutableStateOf<BottomSheetType?>(null) }
    val canDismiss by rememberUpdatedState(dismissEnabled)
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { target -> target != SheetValue.Hidden || canDismiss },
        )
    LaunchedEffect(requestedSheet) {
        if (displayedSheet != requestedSheet) {
            if (displayedSheet != null) sheetState.hide()
            displayedSheet = requestedSheet
        }
    }
    LaunchedEffect(displayedSheet) {
        if (displayedSheet != null) sheetState.show()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        displayedSheet?.let { sheet ->
            key(sheet) {
                ModalBottomSheet(
                    modifier = Modifier.testTag("modal_sheet"),
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    onDismissRequest = {
                        controller.hideBottomSheet()
                        onDismissed()
                    },
                    properties =
                        ModalBottomSheetProperties(
                            shouldDismissOnBackPress = dismissEnabled,
                            shouldDismissOnClickOutside = dismissEnabled,
                        ),
                ) {
                    controller.registry[sheet::class]?.invoke(sheet)
                }
            }
        }
    }
}
