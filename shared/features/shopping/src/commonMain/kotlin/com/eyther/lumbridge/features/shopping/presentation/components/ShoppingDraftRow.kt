package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.CheckCircle
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.checklist_add_item
import org.jetbrains.compose.resources.stringResource

/** The new line typed directly below another row, as in Google Keep. */
@Composable
fun ShoppingDraftRow(
    value: String,
    indented: Boolean,
    onValue: (String) -> Unit,
    onNext: () -> Unit,
    onFinished: () -> Unit,
    onDeleteEmpty: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(start = if (indented) ShoppingIndentation else 0.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckCircle(
            checked = false,
            onCheckedChange = null,
            modifier = Modifier.size(48.dp),
        )
        ShoppingLineInput(
            value = value,
            onValue = onValue,
            onNext = onNext,
            onFocusLost = onFinished,
            onDeleteEmpty = onDeleteEmpty,
            placeholder = stringResource(Res.string.checklist_add_item),
            modifier = Modifier.weight(1f).testTag("shopping_draft_input"),
        )
    }
}
