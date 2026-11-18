package com.eyther.lumbridge.features.settings.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.settings.generated.resources.Res
import com.eyther.lumbridge.features.settings.generated.resources.consent_allow_all
import com.eyther.lumbridge.features.settings.generated.resources.consent_analytics
import com.eyther.lumbridge.features.settings.generated.resources.consent_analytics_description
import com.eyther.lumbridge.features.settings.generated.resources.consent_crashes
import com.eyther.lumbridge.features.settings.generated.resources.consent_crashes_description
import com.eyther.lumbridge.features.settings.generated.resources.consent_footnote
import com.eyther.lumbridge.features.settings.generated.resources.consent_message
import com.eyther.lumbridge.features.settings.generated.resources.consent_required
import com.eyther.lumbridge.features.settings.generated.resources.consent_required_description
import com.eyther.lumbridge.features.settings.generated.resources.consent_retry
import com.eyther.lumbridge.features.settings.generated.resources.consent_save
import com.eyther.lumbridge.features.settings.generated.resources.consent_title
import com.eyther.lumbridge.features.settings.generated.resources.consent_unavailable
import org.jetbrains.compose.resources.stringResource

/** Plannot's independent optional choices, with equally prominent accept and save actions. */
@Composable
fun UsageDataConsentBottomSheet(
    analytics: Boolean,
    crashReports: Boolean,
    analyticsAvailable: Boolean,
    crashReportsAvailable: Boolean,
    ready: Boolean,
    busy: Boolean,
    error: TextResource?,
    onAnalytics: (Boolean) -> Unit,
    onCrashReports: (Boolean) -> Unit,
    onAllowAll: () -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().testTag("usage_data_consent")) {
        Column(
            Modifier.weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.content),
        ) {
            Text(
                stringResource(Res.string.consent_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                stringResource(Res.string.consent_message),
                style = MaterialTheme.typography.bodyMedium,
            )
            ConsentRow(
                stringResource(Res.string.consent_required),
                stringResource(Res.string.consent_required_description),
                true,
                false,
                {},
                "consent_required",
            )
            ConsentRow(
                stringResource(Res.string.consent_crashes),
                stringResource(Res.string.consent_crashes_description),
                crashReports,
                ready && crashReportsAvailable && !busy,
                onCrashReports,
                "consent_crashes",
            )
            if (!crashReportsAvailable)
                Text(
                    stringResource(Res.string.consent_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                )
            ConsentRow(
                stringResource(Res.string.consent_analytics),
                stringResource(Res.string.consent_analytics_description),
                analytics,
                ready && analyticsAvailable && !busy,
                onAnalytics,
                "consent_analytics",
            )
            Text(
                stringResource(Res.string.consent_footnote),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            error?.let { ErrorNotice(message = it) }
            if (!ready && error != null)
                LoomButton(
                    text = stringResource(Res.string.consent_retry),
                    onClick = onRetry,
                    loading = busy,
                )
        }
        Column(
            Modifier.fillMaxWidth().padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
        ) {
            LoomButton(
                stringResource(Res.string.consent_allow_all),
                onAllowAll,
                enabled = ready,
                loading = busy,
                modifier = Modifier.fillMaxWidth().testTag("consent_allow_all"),
            )
            LoomButton(
                stringResource(Res.string.consent_save),
                onSave,
                enabled = ready && !busy,
                modifier = Modifier.fillMaxWidth().testTag("consent_save"),
            )
        }
    }
}

@Composable
private fun ConsentRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onChanged: (Boolean) -> Unit,
    tag: String,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onChanged,
            enabled = enabled,
            modifier = Modifier.testTag(tag),
        )
    }
}
