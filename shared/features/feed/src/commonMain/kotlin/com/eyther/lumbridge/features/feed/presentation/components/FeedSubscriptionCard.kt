package com.eyther.lumbridge.features.feed.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionStatusDomain
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.feed_edit
import com.eyther.lumbridge.features.feed.generated.resources.feed_refresh
import com.eyther.lumbridge.features.feed.generated.resources.feed_remove
import com.eyther.lumbridge.features.feed.generated.resources.feed_status_failed
import com.eyther.lumbridge.features.feed.generated.resources.feed_status_pending
import com.eyther.lumbridge.features.feed.generated.resources.feed_status_ready
import com.eyther.lumbridge.features.feed.generated.resources.feed_status_unsupported
import org.jetbrains.compose.resources.stringResource

private val RefreshTargetSize =
    48.dp // Match the overflow control while an individual feed refreshes.
private val RefreshIndicatorSize = 20.dp

/**
 * Editing stays available for sources with no articles; failures remain visible beside their name.
 */
@Composable
internal fun FeedSubscriptionCard(
    subscription: FeedSubscriptionDomain,
    enabled: Boolean,
    loading: Boolean,
    onEdit: () -> Unit,
    onRefresh: () -> Unit,
    onRemove: () -> Unit,
) {
    GroupedSurface(modifier = Modifier.testTag("feed_subscription_card")) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NavigationRow(
                title = subscription.publisher,
                description =
                    stringResource(
                        when (subscription.status) {
                            FeedSubscriptionStatusDomain.PENDING -> Res.string.feed_status_pending
                            FeedSubscriptionStatusDomain.READY -> Res.string.feed_status_ready
                            FeedSubscriptionStatusDomain.REFRESH_FAILED ->
                                Res.string.feed_status_failed
                            FeedSubscriptionStatusDomain.UNSUPPORTED_PUBLISHER ->
                                Res.string.feed_status_unsupported
                        },
                    ),
                glyph = Glyph.FEED,
                enabled = enabled,
                onClick = onEdit,
                modifier = Modifier.weight(1f).testTag("edit_feed_${subscription.id}"),
            )
            if (loading) {
                Box(
                    modifier = Modifier.size(RefreshTargetSize),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(RefreshIndicatorSize),
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                ActionMenu(
                    modifier = Modifier.testTag("feed_subscription_actions_${subscription.id}"),
                    items =
                        listOf(
                            ActionMenuItem(
                                text = TextResource.Resource(Res.string.feed_edit),
                                onClick = onEdit,
                                enabled = enabled,
                            ),
                            ActionMenuItem(
                                text = TextResource.Resource(Res.string.feed_refresh),
                                onClick = onRefresh,
                                enabled = enabled,
                            ),
                            ActionMenuItem(
                                text = TextResource.Resource(Res.string.feed_remove),
                                onClick = onRemove,
                                enabled = enabled,
                                destructive = true,
                            ),
                        ),
                )
            }
        }
        Text(
            text = subscription.url,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        start = LoomSpacing.content,
                        end = LoomSpacing.content,
                        bottom = LoomSpacing.medium,
                    ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
