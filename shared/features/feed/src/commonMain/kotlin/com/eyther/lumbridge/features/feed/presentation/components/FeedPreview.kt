package com.eyther.lumbridge.features.feed.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.feed.presentation.model.ArticlePreviewUi
import kotlinx.collections.immutable.ImmutableList

internal val FeedPreviewWidth =
    264.dp // Reveal the next card on a narrow phone; large text grows vertically.
internal const val FeedPreviewTitleLines = 3

/** A manually scrolled carousel of [ArticleCard]s; the destination owns opening them. */
@Composable
fun FeedPreview(
    articles: ImmutableList<ArticlePreviewUi>,
    onOpen: (ArticlePreviewUi) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = LoomSpacing.content, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
        modifier = Modifier.fillMaxWidth().testTag("home_feed_preview"),
    ) {
        items(articles, key = { it.id }) { article ->
            ArticleCard(
                article = article,
                onOpen = { onOpen(article) },
                compact = true,
                modifier = Modifier.width(FeedPreviewWidth),
            )
        }
    }
}
