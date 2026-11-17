package com.eyther.lumbridge.features.feed.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.DestructiveButton
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

/** Subscription controls remain available even when no article has ever been fetched. */
@Composable
internal fun FeedSubscriptionCard(
    subscription: FeedSubscriptionDomain,
    enabled: Boolean,
    loading: Boolean,
    onEdit: () -> Unit,
    onRefresh: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().testTag("feed_subscription_card"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier.padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
        ) {
            Text(subscription.publisher, style = MaterialTheme.typography.titleMedium)
            Text(
                subscription.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(
                    when (subscription.status) {
                        FeedSubscriptionStatusDomain.PENDING -> Res.string.feed_status_pending
                        FeedSubscriptionStatusDomain.READY -> Res.string.feed_status_ready
                        FeedSubscriptionStatusDomain.REFRESH_FAILED -> Res.string.feed_status_failed
                        FeedSubscriptionStatusDomain.UNSUPPORTED_PUBLISHER ->
                            Res.string.feed_status_unsupported
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
                FilledTonalButton(onClick = onRefresh, enabled = enabled) {
                    if (loading) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(stringResource(Res.string.feed_refresh))
                }
                OutlinedButton(onClick = onEdit, enabled = enabled) {
                    Text(stringResource(Res.string.feed_edit))
                }
                DestructiveButton(
                    text = stringResource(Res.string.feed_remove),
                    onClick = onRemove,
                    enabled = enabled,
                )
            }
        }
    }
}
