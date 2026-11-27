package com.eyther.lumbridge.features.account.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.domain.AppLanguage
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomDivider
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.RetryNotice
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.picture.rememberPicturePicker
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.account.generated.resources.Res
import com.eyther.lumbridge.features.account.generated.resources.account_deletion_description
import com.eyther.lumbridge.features.account.generated.resources.appearance_title
import com.eyther.lumbridge.features.account.generated.resources.delete_account
import com.eyther.lumbridge.features.account.generated.resources.language_title
import com.eyther.lumbridge.features.account.generated.resources.nickname_description
import com.eyther.lumbridge.features.account.generated.resources.notifications_blocked_description
import com.eyther.lumbridge.features.account.generated.resources.notifications_description
import com.eyther.lumbridge.features.account.generated.resources.notifications_title
import com.eyther.lumbridge.features.account.generated.resources.payment_details_count
import com.eyther.lumbridge.features.account.generated.resources.payment_details_empty
import com.eyther.lumbridge.features.account.generated.resources.payment_details_title
import com.eyther.lumbridge.features.account.generated.resources.preferences_title
import com.eyther.lumbridge.features.account.generated.resources.profile_title
import com.eyther.lumbridge.features.account.generated.resources.retry
import com.eyther.lumbridge.features.account.generated.resources.sign_out
import com.eyther.lumbridge.features.account.generated.resources.ui_nickname_title
import com.eyther.lumbridge.features.account.generated.resources.usage_data_description
import com.eyther.lumbridge.features.account.generated.resources.usage_data_title
import com.eyther.lumbridge.features.account.presentation.components.AccountPreferenceRow
import com.eyther.lumbridge.features.account.presentation.components.MenuPreferenceRow
import com.eyther.lumbridge.features.account.presentation.components.ProfilePictureHeader
import com.eyther.lumbridge.features.account.presentation.mapper.languageLabel
import com.eyther.lumbridge.features.account.presentation.mapper.themeLabel
import com.eyther.lumbridge.features.account.presentation.viewmodel.contracts.AccountScreenViewModelContract
import com.eyther.lumbridge.features.legal.domain.model.LegalDocumentDomain
import com.eyther.lumbridge.features.legal.presentation.components.LegalLinks
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AccountScreen(
    viewModel: AccountScreenViewModelContract,
    onSignOut: () -> Unit,
    onUsageData: () -> Unit,
    onAllowNotifications: () -> Unit,
    notificationsBlocked: Boolean = false,
    onOpenLegalDocument: (LegalDocumentDomain) -> Unit,
    signingOut: Boolean = false,
    signOutError: TextResource? = null,
    onSignOutDismissed: () -> Unit = {},
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    val pickPicture =
        rememberPicturePicker(
            onPicked = viewModel::onPicturePicked,
            onError = viewModel::onPictureError,
        )
    PullToRefreshContentContainer(isRefreshing = state.refreshing, onRefresh = viewModel::onRetry) {
        InnerAccountScreen(
            userId = state.profile?.id.orEmpty(),
            avatarPath = state.profile?.avatarPath,
            hasPicture = state.hasPicture,
            canChangePicture = state.canChangePicture,
            savingPicture = state.savingPicture,
            onChangePicture = pickPicture,
            onRemovePicture = viewModel::onRemovePicture,
            nickname = state.profile?.nickname.orEmpty(),
            canEditNickname = state.canEditNickname,
            loading = state.loading,
            preferences = state.preferences,
            notificationsBlocked = notificationsBlocked,
            theme = state.theme,
            language = state.preferences.language,
            busy = state.busy || signingOut,
            error = state.displayError,
            onEditNickname = viewModel::onEditNickname,
            paymentMethodCount = state.paymentMethodCount,
            onEditPaymentDetails = viewModel::onEditPaymentDetails,
            onTheme = viewModel::onThemeSelected,
            onLanguage = viewModel::onLanguageSelected,
            onNotifications = { enabled ->
                if (enabled) onAllowNotifications() else viewModel.onNotificationsChanged(false)
            },
            onUsageData = onUsageData,
            onRetry = viewModel::onRetry,
            onDismiss = viewModel::onDismissError,
            onSignOut = viewModel::onSignOutRequested,
            onOpenLegalDocument = onOpenLegalDocument,
        )
    }
    state.signOutConfirmation?.let { confirmation ->
        DestructiveConfirmation(
            confirmation = confirmation,
            busy = state.busy || signingOut,
            error = signOutError,
            confirmationTag = "confirm_sign_out",
            onConfirm = onSignOut,
            onDismiss = {
                if (!signingOut) {
                    viewModel.onSignOutCancelled()
                    onSignOutDismissed()
                }
            },
        )
    }
}

@Composable
private fun InnerAccountScreen(
    userId: String,
    avatarPath: String?,
    hasPicture: Boolean,
    canChangePicture: Boolean,
    savingPicture: Boolean,
    onChangePicture: () -> Unit,
    onRemovePicture: () -> Unit,
    nickname: String,
    canEditNickname: Boolean,
    loading: Boolean,
    preferences: SettingsDomain,
    notificationsBlocked: Boolean,
    theme: ThemeMode,
    language: AppLanguage,
    busy: Boolean,
    error: TextResource?,
    onEditNickname: () -> Unit,
    paymentMethodCount: Int,
    onEditPaymentDetails: () -> Unit,
    onTheme: (String) -> Unit,
    onLanguage: (AppLanguage) -> Unit,
    onNotifications: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    onSignOut: () -> Unit,
    onUsageData: () -> Unit,
    onOpenLegalDocument: (LegalDocumentDomain) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("account_content"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        error?.let { message ->
            item {
                RetryNotice(
                    message = message,
                    onRetry = onRetry,
                    onDismiss = onDismiss,
                    retryText = stringResource(Res.string.retry),
                )
            }
        }
        if (!loading)
            item(key = "picture") {
                ProfilePictureHeader(
                    userId = userId,
                    nickname = nickname,
                    avatarPath = avatarPath,
                    hasPicture = hasPicture,
                    enabled = canChangePicture,
                    saving = savingPicture,
                    onChange = onChangePicture,
                    onRemove = onRemovePicture,
                )
            }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(title = stringResource(Res.string.profile_title))
                if (loading) LoadingRows()
                else
                    GroupedSurface {
                        NavigationRow(
                            title =
                                nickname.ifBlank { stringResource(Res.string.ui_nickname_title) },
                            description = stringResource(Res.string.nickname_description),
                            onClick = onEditNickname,
                            glyph = Glyph.PERSON,
                            enabled = canEditNickname,
                            modifier = Modifier.testTag("edit_nickname"),
                        )
                        LoomDivider()
                        NavigationRow(
                            title = stringResource(Res.string.payment_details_title),
                            description =
                                if (paymentMethodCount == 0)
                                    stringResource(Res.string.payment_details_empty)
                                else
                                    pluralStringResource(
                                        Res.plurals.payment_details_count,
                                        paymentMethodCount,
                                        paymentMethodCount,
                                    ),
                            onClick = onEditPaymentDetails,
                            glyph = Glyph.MONEY,
                            enabled = canEditNickname,
                            modifier = Modifier.testTag("edit_payment_details"),
                        )
                    }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(title = stringResource(Res.string.preferences_title))
                GroupedSurface {
                    MenuPreferenceRow(
                        title = stringResource(Res.string.appearance_title),
                        glyph = Glyph.SETTINGS,
                        choices = ThemeMode.entries,
                        selected = theme,
                        label = { choice -> themeLabel(choice) },
                        enabled = !busy,
                        onSelected = { choice -> onTheme(choice.name) },
                        tag = "appearance",
                    )
                    LoomDivider()
                    MenuPreferenceRow(
                        title = stringResource(Res.string.language_title),
                        glyph = Glyph.GLOBE,
                        choices = AppLanguage.entries,
                        selected = language,
                        label = { choice -> languageLabel(choice) },
                        enabled = !busy,
                        onSelected = onLanguage,
                        tag = "language",
                    )
                    LoomDivider()
                    AccountPreferenceRow(
                        title = stringResource(Res.string.notifications_title),
                        glyph = Glyph.BELL,
                        // The phone can block reminders the preference allows; say so and let
                        // the switch lead to settings.
                        description =
                            stringResource(
                                if (notificationsBlocked && preferences.notifications)
                                    Res.string.notifications_blocked_description
                                else Res.string.notifications_description,
                            ),
                        checked = preferences.notifications && !notificationsBlocked,
                        onCheckedChange = onNotifications,
                        enabled = !busy,
                    )
                    LoomDivider()
                    NavigationRow(
                        title = stringResource(Res.string.usage_data_title),
                        description = stringResource(Res.string.usage_data_description),
                        glyph = Glyph.SHIELD,
                        onClick = onUsageData,
                        enabled = !busy,
                        modifier = Modifier.testTag("usage_data_settings"),
                    )
                }
            }
        }
        item {
            LegalLinks(
                onOpenDocument = onOpenLegalDocument,
                showAccountDeletion = false,
                showIntroduction = true,
            )
        }
        item {
            GroupedSurface {
                NavigationRow(
                    title = stringResource(Res.string.sign_out),
                    glyph = Glyph.LOG_OUT,
                    emphasisColor = MaterialTheme.colorScheme.error,
                    onClick = onSignOut,
                    enabled = !busy,
                    modifier = Modifier.testTag("sign_out"),
                )
            }
        }
        item {
            GroupedSurface {
                NavigationRow(
                    title = stringResource(Res.string.delete_account),
                    description = stringResource(Res.string.account_deletion_description),
                    glyph = Glyph.DELETE,
                    emphasisColor = MaterialTheme.colorScheme.error,
                    onClick = { onOpenLegalDocument(LegalDocumentDomain.ACCOUNT_DELETION) },
                    enabled = !busy,
                    modifier = Modifier.testTag("delete_account"),
                )
            }
        }
    }
}
