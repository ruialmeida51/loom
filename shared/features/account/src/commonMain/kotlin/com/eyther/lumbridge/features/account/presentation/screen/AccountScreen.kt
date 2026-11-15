package com.eyther.lumbridge.features.account.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.account.generated.resources.Res
import com.eyther.lumbridge.features.account.generated.resources.analytics_description
import com.eyther.lumbridge.features.account.generated.resources.analytics_title
import com.eyther.lumbridge.features.account.generated.resources.appearance_title
import com.eyther.lumbridge.features.account.generated.resources.nickname_description
import com.eyther.lumbridge.features.account.generated.resources.notifications_description
import com.eyther.lumbridge.features.account.generated.resources.notifications_title
import com.eyther.lumbridge.features.account.generated.resources.preferences_title
import com.eyther.lumbridge.features.account.generated.resources.profile_title
import com.eyther.lumbridge.features.account.generated.resources.retry
import com.eyther.lumbridge.features.account.generated.resources.sign_out
import com.eyther.lumbridge.features.account.generated.resources.ui_nickname_title
import com.eyther.lumbridge.features.account.presentation.components.AccountPreferenceRow
import com.eyther.lumbridge.features.account.presentation.mapper.themeLabel
import com.eyther.lumbridge.features.account.presentation.viewmodel.contracts.AccountScreenViewModelContract
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsPreferencesDomain
import com.eyther.lumbridge.features.legal.domain.model.LegalDocumentDomain
import com.eyther.lumbridge.features.legal.presentation.components.LegalLinks
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import org.jetbrains.compose.resources.stringResource

@Composable
fun AccountScreen(
    viewModel: AccountScreenViewModelContract,
    onSignOut: () -> Unit,
    onOpenLegalDocument: (LegalDocumentDomain) -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerAccountScreen(
        nickname = state.profile?.nickname.orEmpty(),
        canEditNickname = state.canEditNickname,
        loading = state.loading,
        preferences = state.preferences,
        analytics = state.analytics,
        busy = state.busy,
        error = state.error,
        onEditNickname = viewModel::onEditNickname,
        onTheme = viewModel::onThemeSelected,
        onNotifications = viewModel::onNotificationsChanged,
        onAnalyticsConsent = viewModel::onAnalyticsConsentChanged,
        onRetry = viewModel::onRetry,
        onDismiss = viewModel::onDismissError,
        onSignOut = onSignOut,
        onOpenLegalDocument = onOpenLegalDocument,
    )
}

@Composable
private fun InnerAccountScreen(
    nickname: String,
    canEditNickname: Boolean,
    loading: Boolean,
    preferences: SettingsDomain,
    analytics: AnalyticsPreferencesDomain,
    busy: Boolean,
    error: TextResource?,
    onEditNickname: () -> Unit,
    onTheme: (String) -> Unit,
    onNotifications: (Boolean) -> Unit,
    onAnalyticsConsent: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
    onOpenLegalDocument: (LegalDocumentDomain) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("account_content"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        error?.let { message ->
            item {
                ErrorNotice(message = message, onDismiss = onDismiss)
                TextButton(onClick = onRetry) { Text(stringResource(Res.string.retry)) }
            }
        }
        item {
            SectionTitle(title = stringResource(Res.string.profile_title))
            if (loading) LoadingRows()
            else
                GroupedSurface {
                    NavigationRow(
                        title = nickname.ifBlank { stringResource(Res.string.ui_nickname_title) },
                        description = stringResource(Res.string.nickname_description),
                        onClick = onEditNickname,
                        enabled = canEditNickname,
                        modifier = Modifier.testTag("edit_nickname"),
                    )
                }
        }
        item {
            SectionTitle(title = stringResource(Res.string.preferences_title))
            GroupedSurface {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(Res.string.appearance_title),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { theme ->
                            FilterChip(
                                selected = preferences.theme == theme.name,
                                onClick = { onTheme(theme.name) },
                                label = { Text(themeLabel(theme)) },
                            )
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                AccountPreferenceRow(
                    title = stringResource(Res.string.notifications_title),
                    description = stringResource(Res.string.notifications_description),
                    checked = preferences.notifications,
                    onCheckedChange = onNotifications,
                    enabled = !busy,
                )
                if (analytics.available) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    AccountPreferenceRow(
                        title = stringResource(Res.string.analytics_title),
                        description = stringResource(Res.string.analytics_description),
                        checked = analytics.enabled,
                        onCheckedChange = onAnalyticsConsent,
                        enabled = !busy,
                        modifier = Modifier.testTag("analytics_consent"),
                    )
                }
            }
        }
        item { LegalLinks(onOpenDocument = onOpenLegalDocument, showAccountDeletion = true) }
        item {
            OutlinedButton(
                onClick = onSignOut,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.sign_out))
            }
        }
    }
}
