package com.eyther.lumbridge.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.app.navigation.components.ScreenAnalyticsEffect
import com.eyther.lumbridge.app.navigation.sheet.TasksSheetHost
import com.eyther.lumbridge.core.presentation.components.FloatingActionContentPadding
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.tasks.presentation.screen.TasksScreen
import com.eyther.lumbridge.features.tasks.presentation.viewmodel.contracts.TasksScreenViewModelContract
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun TasksHost(
    context: HouseholdContextDomain,
    portalToolbar: @Composable () -> Unit,
    onRootChanged: (Boolean) -> Unit,
) {
    val viewModel: TasksScreenViewModelContract = koinViewModel()
    LaunchedEffect(Unit) { onRootChanged(true) }
    ScreenAnalyticsEffect(screen = AnalyticsScreenDomain.TASKS)
    Scaffold(topBar = portalToolbar) { padding ->
        Box(Modifier.padding(padding).consumeWindowInsets(padding)) {
            TasksSheetHost(
                viewModel = viewModel,
                timeZone = context.timeZone,
                snackbarBottomPadding = FloatingActionContentPadding,
                primeOnOpen = true,
            ) {
                TasksScreen(viewModel = viewModel)
            }
        }
    }
}
