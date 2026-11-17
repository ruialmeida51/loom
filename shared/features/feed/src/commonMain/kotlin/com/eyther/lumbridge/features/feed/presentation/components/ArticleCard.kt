package com.eyther.lumbridge.features.feed.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.ui_mark_as_read
import com.eyther.lumbridge.features.feed.generated.resources.ui_mark_unread
import com.eyther.lumbridge.features.feed.presentation.model.ArticlePreviewUi

@Composable
internal fun ArticleCard(article: ArticlePreviewUi, onOpen: () -> Unit, onReadChanged: () -> Unit) {
    Surface(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier.padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
        ) {
            Row {
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
                ) {
                    Text(
                        article.source,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    article.publishedOn?.let {
                        Text(
                            calendarDateText(it),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                ActionMenu(
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
            Text(
                article.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (article.summary.isNotBlank())
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
