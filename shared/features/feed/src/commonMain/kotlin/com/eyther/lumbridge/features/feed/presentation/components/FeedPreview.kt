package com.eyther.lumbridge.features.feed.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.feed.presentation.model.ArticlePreviewUi
import kotlinx.collections.immutable.ImmutableList

internal val FeedPreviewWidth =
    248.dp // Reveal the next card on a narrow phone; large text grows vertically.
internal const val FeedPreviewTitleLines = 3

/** A small, manually scrolled article preview; the destination owns opening and read state. */
@Composable
fun FeedPreview(articles: ImmutableList<ArticlePreviewUi>, onOpen: (ArticlePreviewUi) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = LoomSpacing.content),
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
        modifier = Modifier.fillMaxWidth().testTag("home_feed_preview"),
    ) {
        items(articles, key = { it.id }) { article ->
            Surface(
                onClick = { onOpen(article) },
                modifier = Modifier.width(FeedPreviewWidth),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Column(
                    Modifier.padding(LoomSpacing.content),
                    verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LoomIcon(
                            glyph = Glyph.FEED,
                            tint = MaterialTheme.colorScheme.primary,
                            decorative = true,
                        )
                        Text(
                            article.source,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        article.title,
                        style = MaterialTheme.typography.titleMedium,
                        minLines = FeedPreviewTitleLines,
                        maxLines = FeedPreviewTitleLines,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        article.publishedOn?.let { date ->
                            Text(
                                calendarDateText(date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        LoomIcon(glyph = Glyph.CHEVRON_RIGHT, decorative = true, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
