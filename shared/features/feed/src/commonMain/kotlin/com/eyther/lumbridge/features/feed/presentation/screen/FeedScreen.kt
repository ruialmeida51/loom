package com.eyther.lumbridge.features.feed.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.generated.resources.*
import com.eyther.lumbridge.features.feed.presentation.localization.localizedMessage
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedScreen(vm: FeedScreenViewModelContract, onOpen: (String) -> Unit) {
    val state by vm.state.platformCollectAsStateOnStart()
    InnerFeedScreen(
        items = state.items,
        url = state.url,
        showFollow = state.showFollow,
        unreadOnly = state.unreadOnly,
        busy = state.busy,
        error = state.error,
        onOpen = onOpen,
        onUrl = vm::url,
        onShowFollow = vm::showFollow,
        onUnreadOnly = vm::unreadOnly,
        onDismiss = vm::dismiss,
        onFollow = vm::follow,
        onMarkRead = vm::markRead,
    )
}

@Composable
private fun InnerFeedScreen(
    items: List<ArticleDomain>,
    url: String,
    showFollow: Boolean,
    unreadOnly: Boolean,
    busy: Boolean,
    error: String?,
    onOpen: (String) -> Unit,
    onUrl: (String) -> Unit,
    onShowFollow: (Boolean) -> Unit,
    onUnreadOnly: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onFollow: () -> Unit,
    onMarkRead: (ArticleDomain, Boolean) -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageHeading(
                stringResource(Res.string.ui_a_moment_for_you),
                stringResource(Res.string.ui_the_reading_corner),
                stringResource(Res.string.ui_your_favourite_corners_of_the_internet_in_one_quiet),
            )
        }
        item { ErrorNotice(localizedMessage(error), onDismiss) }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    unreadOnly,
                    { onUnreadOnly(!unreadOnly) },
                    label = { Text(stringResource(Res.string.ui_unread_only)) },
                )
                TextButton(onClick = { onShowFollow(true) }) {
                    Text(stringResource(Res.string.ui_follow_a_feed))
                }
            }
        }
        val articles = items.filter { !unreadOnly || !it.read }
        if (articles.isEmpty())
            item {
                EmptyNotice(
                    stringResource(Res.string.ui_a_fresh_page),
                    stringResource(Res.string.ui_follow_an_rss_or_atom_feed_to_bring_your),
                )
            }
        items(articles, key = { it.id }) { article ->
            LoomCard {
                Text(
                    article.source.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(article.title, style = MaterialTheme.typography.headlineMedium)
                Text(
                    article.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { onMarkRead(article, !article.read) }) {
                        Text(
                            if (article.read) stringResource(Res.string.ui_mark_unread)
                            else stringResource(Res.string.ui_mark_as_read),
                        )
                    }
                    TextButton(
                        onClick = {
                            onMarkRead(article, true)
                            onOpen(article.url)
                        },
                    ) {
                        Text(stringResource(Res.string.ui_read_story))
                        Spacer(Modifier.width(6.dp))
                        LoomIcon(
                            Glyph.ARROW,
                            Modifier.size(16.dp),
                            MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
    if (showFollow)
        AlertDialog(
            onDismissRequest = { if (!busy) onShowFollow(false) },
            title = { Text(stringResource(Res.string.ui_follow_a_feed_a533eb8)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(Res.string.ui_paste_an_rss_or_atom_feed_url_articles_will))
                    OutlinedTextField(
                        url,
                        onUrl,
                        label = { Text(stringResource(Res.string.ui_feed_url_hint)) },
                        singleLine = true,
                    )
                    ErrorNotice(localizedMessage(error), onDismiss)
                }
            },
            confirmButton = {
                LoomButton(stringResource(Res.string.ui_follow_feed), onFollow, loading = busy)
            },
            dismissButton = {
                TextButton(onClick = { onShowFollow(false) }, enabled = !busy) {
                    Text(stringResource(Res.string.ui_cancel))
                }
            },
        )
}
