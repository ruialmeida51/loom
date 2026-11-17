package com.eyther.lumbridge.features.feed.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.feed_all_sources
import com.eyther.lumbridge.features.feed.generated.resources.ui_unread_only
import com.eyther.lumbridge.features.feed.presentation.model.FeedSourceFilterUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FeedSourceFilters(
    sources: ImmutableList<FeedSourceFilterUi>,
    selectedIds: ImmutableSet<String>,
    unreadOnly: Boolean,
    onSource: (String) -> Unit,
    onAll: () -> Unit,
    onUnreadOnly: (Boolean) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
        modifier = Modifier.testTag("feed_source_filters"),
    ) {
        item(key = "all") {
            FilterChip(
                selected = selectedIds.isEmpty(),
                onClick = onAll,
                label = { Text(stringResource(Res.string.feed_all_sources)) },
                modifier = Modifier.testTag("feed_all_sources"),
            )
        }
        item(key = "unread") {
            FilterChip(
                selected = unreadOnly,
                onClick = { onUnreadOnly(!unreadOnly) },
                label = { Text(stringResource(Res.string.ui_unread_only)) },
                modifier = Modifier.testTag("feed_unread_filter"),
            )
        }
        items(items = sources, key = { it.id }) { source ->
            FilterChip(
                selected = source.id in selectedIds,
                onClick = { onSource(source.id) },
                label = { Text(source.label) },
                modifier = Modifier.testTag("feed_source_${source.id}"),
            )
        }
    }
}
