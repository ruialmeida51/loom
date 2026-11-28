package com.eyther.lumbridge.features.feed.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.IconTile
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.components.picture.RemotePicture
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.presentation.model.ArticlePreviewUi
import org.jetbrains.compose.resources.stringResource

internal val ArticlePictureHeight = 108.dp
private const val ArticlePictureRatio = 2.4f

/**
 * One article as a single tap target: tapping anywhere opens it. [compact] is the Home carousel variant, which reserves title lines and the picture so
 * cards align; in the list, only stories with a picture show one.
 */
@Composable
fun ArticleCard(
    article: ArticlePreviewUi,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val hasPicture = compact || article.imageUrl != null
    LoomCard(
        onClick = onOpen,
        modifier = modifier.testTag("article_card_${article.id}"),
        contentPadding = PaddingValues(),
        verticalArrangement = Arrangement.Top,
    ) {
        if (hasPicture)
            ArticlePicture(
                path = article.imageUrl,
                modifier =
                    if (compact) Modifier.fillMaxWidth().height(ArticlePictureHeight)
                    else Modifier.fillMaxWidth().aspectRatio(ArticlePictureRatio),
            )
        Column(
            Modifier.padding(start = LoomSpacing.content, bottom = LoomSpacing.content, top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!hasPicture) IconTile(glyph = Glyph.FEED, size = 32.dp)
                Column(Modifier.weight(1f).padding(start = if (hasPicture) 0.dp else 10.dp)) {
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
                    }
                    article.publishedOn?.let {
                        Text(
                            calendarDateText(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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

/** The publisher's picture, or a soft tile while it loads and when there is none. */
@Composable
private fun ArticlePicture(path: String?, modifier: Modifier) {
    RemotePicture(
        path = path,
        shape = RectangleShape,
        modifier = modifier.testTag(if (path == null) "article_picture_none" else "article_picture"),
    ) {
        Box(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            LoomIcon(
                glyph = Glyph.FEED,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                decorative = true,
            )
        }
    }
}
