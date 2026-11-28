package com.eyther.lumbridge.features.feed.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.PillChip
import com.eyther.lumbridge.core.presentation.components.bleedHorizontally
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.feed_all_sources
import com.eyther.lumbridge.features.feed.presentation.model.FeedSourceFilterUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FeedSourceFilters(
    sources: ImmutableList<FeedSourceFilterUi>,
    selectedIds: ImmutableSet<String>,
    onSource: (String) -> Unit,
    onAll: () -> Unit,
) {
    // The same pills as shopping lists and Money, scrolling to the screen edges.
    LazyRow(
        contentPadding = PaddingValues(horizontal = LoomSpacing.content, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
        modifier =
            Modifier.bleedHorizontally(LoomSpacing.content).testTag("feed_source_filters"),
    ) {
        item(key = "all") {
            PillChip(
                text = stringResource(Res.string.feed_all_sources),
                selected = selectedIds.isEmpty(),
                onClick = onAll,
                role = Role.Checkbox,
                modifier = Modifier.testTag("feed_all_sources"),
            )
        }
        items(items = sources, key = { it.id }) { source ->
            PillChip(
                text = source.label,
                selected = source.id in selectedIds,
                onClick = { onSource(source.id) },
                role = Role.Checkbox,
                modifier = Modifier.testTag("feed_source_${source.id}"),
            )
        }
    }
}
