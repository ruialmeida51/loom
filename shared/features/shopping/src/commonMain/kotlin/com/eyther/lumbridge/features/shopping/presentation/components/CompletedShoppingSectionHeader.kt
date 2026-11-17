package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.eyther.lumbridge.core.presentation.components.SelectorButton
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.checklist_completed
import com.eyther.lumbridge.features.shopping.generated.resources.completed_cancel_selection
import com.eyther.lumbridge.features.shopping.generated.resources.completed_collapsed
import com.eyther.lumbridge.features.shopping.generated.resources.completed_expanded
import com.eyther.lumbridge.features.shopping.generated.resources.completed_select
import com.eyther.lumbridge.features.shopping.generated.resources.completed_select_short
import com.eyther.lumbridge.features.shopping.generated.resources.ui_done
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** A checklist section disclosure; selection remains a separate, explicitly labelled action. */
@Composable
internal fun CompletedShoppingSectionHeader(
    count: Int,
    expanded: Boolean,
    selecting: Boolean,
    busy: Boolean,
    onExpanded: (Boolean) -> Unit,
    onSelecting: (Boolean) -> Unit,
) {
    val expansionState =
        stringResource(
            if (expanded) Res.string.completed_expanded else Res.string.completed_collapsed,
        )
    val selectionLabel =
        stringResource(
            if (selecting) Res.string.completed_cancel_selection else Res.string.completed_select,
        )
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = LoomSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SelectorButton(
                text = pluralStringResource(Res.plurals.checklist_completed, count, count),
                onClick = { onExpanded(!expanded) },
                expanded = expanded,
                textStyle = MaterialTheme.typography.labelLarge,
                modifier =
                    Modifier.weight(1f).testTag("shopping_completed").semantics {
                        stateDescription = expansionState
                    },
            )
            FilledTonalButton(
                onClick = { onSelecting(!selecting) },
                enabled = !busy,
                modifier =
                    Modifier.testTag("select_completed").semantics {
                        contentDescription = selectionLabel
                    },
            ) {
                Text(
                    stringResource(
                        if (selecting) Res.string.ui_done else Res.string.completed_select_short,
                    ),
                )
            }
        }
    }
}
