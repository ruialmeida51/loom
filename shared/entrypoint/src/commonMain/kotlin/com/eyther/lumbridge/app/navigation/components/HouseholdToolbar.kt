package com.eyther.lumbridge.app.navigation.components

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.eyther.lumbridge.app.navigation.model.HouseholdDestination
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.components.toolbar.Toolbar
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.presentation.components.HouseholdBadge
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.token_home
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_insights
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_recurring_expenses
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_reminders
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_settings
import com.eyther.lumbridge.shared.entrypoint.generated.resources.toolbar_switch_household
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_feed
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_money
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_shopping
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HouseholdToolbar(
    destination: HouseholdDestination,
    context: HouseholdContextDomain,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onSwitchHousehold: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val title = when (destination) {
        HouseholdDestination.Home -> Res.string.token_home
        HouseholdDestination.Feed -> Res.string.ui_feed
        HouseholdDestination.Shopping -> Res.string.ui_shopping
        HouseholdDestination.Money -> Res.string.ui_money
        HouseholdDestination.Reminders -> Res.string.toolbar_reminders
        HouseholdDestination.RecurringExpenses -> Res.string.toolbar_recurring_expenses
        HouseholdDestination.Insights -> Res.string.toolbar_insights
        HouseholdDestination.Settings -> Res.string.toolbar_settings
    }
    val switchHouseholdLabel = stringResource(Res.string.toolbar_switch_household, context.name)
    val actions = buildList {
        if (destination != HouseholdDestination.Reminders) {
            add(
                ActionMenuItem(
                    text = TextResource.Resource(Res.string.toolbar_reminders),
                    onClick = onOpenReminders,
                ),
            )
        }
        if (destination != HouseholdDestination.Settings) {
            add(
                ActionMenuItem(
                    text = TextResource.Resource(Res.string.toolbar_settings),
                    onClick = onOpenSettings,
                ),
            )
        }
    }

    Toolbar(
        text = stringResource(title),
        subtitle = context.name,
        showBackButton = showBackButton,
        onBack = onBack,
        actions = {
            IconButton(
                onClick = onSwitchHousehold,
                modifier = Modifier
                    .testTag("household_switch")
                    .semantics { contentDescription = switchHouseholdLabel },
            ) {
                HouseholdBadge(id = context.id, name = context.name)
            }
            ActionMenu(items = actions)
        },
    )
}
