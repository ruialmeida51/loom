package com.eyther.lumbridge.features.shopping.presentation.components.bottomsheet

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoomDivider
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.move_description
import com.eyther.lumbridge.features.shopping.generated.resources.move_title
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

/** Immediate selection: choosing a list moves the item and closes the sheet. */
@Composable
fun ShoppingMoveBottomSheet(
    itemName: String,
    targets: ImmutableList<ShoppingListDomain>,
    busy: Boolean,
    onMove: (String) -> Unit,
) {
    FormBottomSheet(title = stringResource(Res.string.move_title)) {
        Text(
            stringResource(Res.string.move_description, itemName),
            style = MaterialTheme.typography.bodyMedium,
        )
        GroupedSurface {
            targets.forEachIndexed { index, target ->
                key(target.id) {
                    if (index > 0) LoomDivider()
                    NavigationRow(
                        title = target.name,
                        glyph = Glyph.SHOPPING,
                        enabled = !busy,
                        onClick = { onMove(target.id) },
                        modifier = Modifier.testTag("move_to_${target.name}"),
                    )
                }
            }
        }
    }
}
