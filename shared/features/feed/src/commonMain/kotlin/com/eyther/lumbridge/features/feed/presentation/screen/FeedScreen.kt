package com.eyther.lumbridge.features.feed.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.presentation.viewmodel.contracts.FeedScreenViewModelContract
import kotlinx.coroutines.flow.*

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
                "A MOMENT FOR YOU",
                "The reading corner",
                "Your favourite corners of the internet, in one quiet place.",
            )
        }
        item { ErrorNotice(error, onDismiss) }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    unreadOnly,
                    { onUnreadOnly(!unreadOnly) },
                    label = { Text("Unread only") },
                )
                TextButton(onClick = { onShowFollow(true) }) { Text("+ Follow a feed") }
            }
        }
        val articles = items.filter { !unreadOnly || !it.read }
        if (articles.isEmpty())
            item {
                EmptyNotice(
                    "A fresh page.",
                    "Follow an RSS or Atom feed to bring your favourite stories here.",
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
                        Text(if (article.read) "Mark unread" else "Mark as read")
                    }
                    TextButton(
                        onClick = {
                            onMarkRead(article, true)
                            onOpen(article.url)
                        },
                    ) {
                        Text("Read story")
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
            title = { Text("Follow a feed") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Paste an RSS or Atom feed URL. Articles will refresh automatically.")
                    OutlinedTextField(url, onUrl, label = { Text("https://…") }, singleLine = true)
                    ErrorNotice(error, onDismiss)
                }
            },
            confirmButton = { LoomButton("Follow feed", onFollow, loading = busy) },
            dismissButton = {
                TextButton(onClick = { onShowFollow(false) }, enabled = !busy) { Text("Cancel") }
            },
        )
}
