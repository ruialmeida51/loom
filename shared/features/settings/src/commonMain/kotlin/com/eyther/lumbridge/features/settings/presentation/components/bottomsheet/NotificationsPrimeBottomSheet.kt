package com.eyther.lumbridge.features.settings.presentation.components.bottomsheet

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain
import com.eyther.lumbridge.features.settings.generated.resources.Res
import com.eyther.lumbridge.features.settings.generated.resources.notifications_prime_enable
import com.eyther.lumbridge.features.settings.generated.resources.notifications_prime_footnote
import com.eyther.lumbridge.features.settings.generated.resources.notifications_prime_denied_message
import com.eyther.lumbridge.features.settings.generated.resources.notifications_prime_message
import com.eyther.lumbridge.features.settings.generated.resources.notifications_prime_not_now
import com.eyther.lumbridge.features.settings.generated.resources.notifications_prime_settings
import com.eyther.lumbridge.features.settings.generated.resources.notifications_prime_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotificationsPrimeBottomSheet(
    permission: NotificationPermissionDomain,
    busy: Boolean,
    error: TextResource?,
    onEnable: () -> Unit,
    onNotNow: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.notifications_prime_title),
        onAction = onEnable,
        onDismiss = onNotNow,
        actionLabel =
            stringResource(
                if (permission == NotificationPermissionDomain.DENIED)
                    Res.string.notifications_prime_settings
                else Res.string.notifications_prime_enable,
            ),
        dismissLabel = stringResource(Res.string.notifications_prime_not_now),
        actionEnabled = permission != NotificationPermissionDomain.UNAVAILABLE,
        busy = busy,
        actionModifier = Modifier.testTag("notifications_enable"),
    ) {
        Text(
            stringResource(
                if (permission == NotificationPermissionDomain.DENIED)
                    Res.string.notifications_prime_denied_message
                else Res.string.notifications_prime_message,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            stringResource(Res.string.notifications_prime_footnote),
            style = MaterialTheme.typography.bodySmall,
        )
        error?.let { ErrorNotice(message = it) }
    }
}
