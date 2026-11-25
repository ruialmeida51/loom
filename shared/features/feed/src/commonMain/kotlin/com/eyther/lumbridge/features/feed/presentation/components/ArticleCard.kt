package com.eyther.lumbridge.features.feed.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.IconTile
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.article_options
import com.eyther.lumbridge.features.feed.generated.resources.article_unread
import com.eyther.lumbridge.features.feed.generated.resources.ui_mark_as_read
import com.eyther.lumbridge.features.feed.generated.resources.ui_mark_unread
import com.eyther.lumbridge.features.feed.presentation.model.ArticlePreviewUi
import org.jetbrains.compose.resources.stringResource

private val UnreadDotSize = 8.dp

/**
 * One article as a single tap target: tapping anywhere opens it, and the ⋮ menu holds the other
 * actions. [compact] is the Home carousel variant, which reserves title lines so cards align.
 */
@Composable
fun ArticleCard(
    article: ArticlePreviewUi,
    onOpen: () -> Unit,
    onReadChanged: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val unread = stringResource(Res.string.article_unread)
    Surface(
        onClick = onOpen,
        modifier = modifier.testTag("article_card_${article.id}"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = if (LoomColors.cardShadow) 1.dp else 0.dp,
        border = BorderStroke(1.dp, LoomColors.cardBorder),
    ) {
        Column(
            Modifier.padding(start = LoomSpacing.content, bottom = LoomSpacing.content, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(glyph = Glyph.FEED, size = 32.dp)
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            article.source,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (!article.read)
                            Box(
                                Modifier.size(UnreadDotSize)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .semantics { contentDescription = unread },
                            )
                    }
                    article.publishedOn?.let {
                        Text(
                            calendarDateText(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                ActionMenu(
                    contentDescription = stringResource(Res.string.article_options, article.title),
                    modifier = Modifier.testTag("feed_article_actions_${article.id}"),
                    items =
                        listOf(
                            ActionMenuItem(
                                text =
                                    TextResource.Resource(
                                        if (article.read) Res.string.ui_mark_unread
                                        else Res.string.ui_mark_as_read,
                                    ),
                                onClick = onReadChanged,
                            ),
                        ),
                )
            }
            Column(
                Modifier.padding(end = LoomSpacing.content),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    article.title,
                    style = MaterialTheme.typography.titleMedium,
                    minLines = if (compact) FeedPreviewTitleLines else 1,
                    maxLines = FeedPreviewTitleLines,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!compact && article.summary.isNotBlank())
                    Text(
                        article.summary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
            }
        }
    }
}
