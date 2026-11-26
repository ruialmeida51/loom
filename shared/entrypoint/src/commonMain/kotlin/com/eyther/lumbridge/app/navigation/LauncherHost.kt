package com.eyther.lumbridge.app.navigation

import com.eyther.lumbridge.features.expenses.domain.datasource.ExchangeRateDataSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalUriHandler
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.authentication.presentation.screen.SessionScreen
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.SessionScreenViewModelContract
import com.eyther.lumbridge.features.diagnostics.domain.datasource.DiagnosticsDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.legal.domain.usecase.GetLegalDocumentsUseCase
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Composes the navigation graphs; the authentication feature owns session and household lifecycle.
 */
@Composable
fun LauncherHost() {
    val viewModel: SessionScreenViewModelContract = koinViewModel()
    val legalDocuments: GetLegalDocumentsUseCase = koinInject()
    val access: SupabaseAccess = koinInject()
    val householdDataSource: HouseholdDataSource = koinInject()
    val profileDataSource: ProfileDataSource = koinInject()
    val analyticsDataSource: AnalyticsDataSource = koinInject()
    val diagnosticsDataSource: DiagnosticsDataSource = koinInject()
    val exchangeRateDataSource = koinInject<ExchangeRateDataSource>()
    val uriHandler = LocalUriHandler.current
    SessionScreen(
        viewModel = viewModel,
        signedOutContent = {
            ScreenAnalyticsEffect(screen = AnalyticsScreenDomain.AUTHENTICATION)
            AuthenticationHost(
                onOpenLegalDocument = { document ->
                    uriHandler.openUri(legalDocuments().url(document = document))
                },
            )
        },
        authenticatedContent = { userId, context, signingOut, signOutError ->
            key(userId) {
                PortalHost(
                    userId = userId,
                    context = context,
                    access = access,
                    householdDataSource = householdDataSource,
                    profileDataSource = profileDataSource,
                    analyticsDataSource = analyticsDataSource,
                    diagnosticsDataSource = diagnosticsDataSource,
                    exchangeRateDataSource = exchangeRateDataSource,
                    signingOut = signingOut,
                    signOutError = signOutError,
                    onSelect = viewModel::onHouseholdSelected,
                    onSignOut = viewModel::onSignOut,
                    onHouseholdExited = viewModel::onHouseholdExited,
                    onSignOutDismissed = viewModel::onSignOutDismissed,
                )
            }
        },
    )
}
