package com.eyther.lumbridge.features.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.EmptyNotice
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.presentation.model.BalanceSummaryUi
import com.eyther.lumbridge.features.home.generated.resources.Res
import com.eyther.lumbridge.features.home.generated.resources.shopping_item_count
import com.eyther.lumbridge.features.home.generated.resources.ui_a_clear_calendar
import com.eyther.lumbridge.features.home.generated.resources.ui_a_gentle_reminder
import com.eyther.lumbridge.features.home.generated.resources.ui_a_moment_for_yourself
import com.eyther.lumbridge.features.home.generated.resources.ui_all_settled
import com.eyther.lumbridge.features.home.generated.resources.ui_enjoy_a_little_breathing_room
import com.eyther.lumbridge.features.home.generated.resources.ui_fresh_reads_from_the_places_you_love_put_the
import com.eyther.lumbridge.features.home.generated.resources.ui_home_in_harmony
import com.eyther.lumbridge.features.home.generated.resources.ui_less_to_keep_in_your_head_more_space_for
import com.eyther.lumbridge.features.home.generated.resources.ui_on_the_shopping_list
import com.eyther.lumbridge.features.home.generated.resources.ui_open_list
import com.eyther.lumbridge.features.home.generated.resources.ui_pick_up_a_few_things
import com.eyther.lumbridge.features.home.generated.resources.ui_see_all
import com.eyther.lumbridge.features.home.generated.resources.ui_shopping_clear
import com.eyther.lumbridge.features.home.generated.resources.ui_shopping_clear_description
import com.eyther.lumbridge.features.home.generated.resources.ui_visit_the_reading_corner
import com.eyther.lumbridge.features.home.generated.resources.ui_you_owe
import com.eyther.lumbridge.features.home.generated.resources.ui_you_re_owed
import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi
import com.eyther.lumbridge.features.home.presentation.model.HomeShoppingUi
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModelContract,
    context: HouseholdContextDomain,
    onShopping: () -> Unit,
    onReminders: () -> Unit,
    onMoney: () -> Unit,
    onFeed: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerHomeScreen(
        loading = state.loading,
        shoppingCount = state.shoppingCount,
        shopping = state.shopping,
        reminders = state.reminders,
        balance = state.balance,
        error = state.error,
        onShopping = onShopping,
        onReminders = onReminders,
        onMoney = onMoney,
        onFeed = onFeed,
        onDismiss = viewModel::onDismissError,
    )
}

@Composable
private fun InnerHomeScreen(
    loading: Boolean,
    shoppingCount: Int,
    shopping: List<HomeShoppingUi>,
    reminders: List<HomeReminderUi>,
    balance: BalanceSummaryUi,
    error: TextResource?,
    onShopping: () -> Unit,
    onReminders: () -> Unit,
    onMoney: () -> Unit,
    onFeed: () -> Unit,
    onDismiss: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).testTag("home_content"),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            PageHeading(
                title = stringResource(Res.string.ui_home_in_harmony),
                subtitle = stringResource(Res.string.ui_less_to_keep_in_your_head_more_space_for),
            )
        }
        error?.let { message ->
            item(key = "error") { ErrorNotice(message = message, onDismiss = onDismiss) }
        }
        if (loading) {
            item { LoadingRows() }
        } else {
            item {
                GroupedSurface {
                    NavigationRow(
                        title =
                            pluralStringResource(
                                Res.plurals.shopping_item_count,
                                shoppingCount,
                                shoppingCount,
                            ),
                        description = stringResource(Res.string.ui_on_the_shopping_list),
                        onClick = onShopping,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationRow(
                        title = balance.amount,
                        description =
                            when {
                                balance.money.minorUnits == 0L ->
                                    stringResource(Res.string.ui_all_settled)
                                balance.isOwed -> stringResource(Res.string.ui_you_re_owed)
                                else -> stringResource(Res.string.ui_you_owe)
                            },
                        onClick = onMoney,
                    )
                }
            }
            item {
                SectionTitle(
                    title = stringResource(Res.string.ui_a_gentle_reminder),
                    action = stringResource(Res.string.ui_see_all),
                    onAction = onReminders,
                )
                if (reminders.isEmpty()) {
                    EmptyNotice(
                        title = stringResource(Res.string.ui_a_clear_calendar),
                        description = stringResource(Res.string.ui_enjoy_a_little_breathing_room),
                    )
                } else {
                    GroupedSurface {
                        reminders.forEachIndexed { index, reminder ->
                            if (index > 0)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            NavigationRow(
                                title = reminder.title,
                                description = reminder.due,
                                onClick = onReminders,
                            )
                        }
                    }
                }
            }
            item {
                SectionTitle(
                    title = stringResource(Res.string.ui_pick_up_a_few_things),
                    action = stringResource(Res.string.ui_open_list),
                    onAction = onShopping,
                )
                if (shopping.isEmpty()) {
                    EmptyNotice(
                        title = stringResource(Res.string.ui_shopping_clear),
                        description = stringResource(Res.string.ui_shopping_clear_description),
                        modifier = Modifier.testTag("home_shopping_empty"),
                    )
                } else {
                    GroupedSurface(modifier = Modifier.testTag("home_shopping_preview")) {
                        shopping.forEachIndexed { index, item ->
                            if (index > 0)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            NavigationRow(
                                title = item.name,
                                description = item.quantity.takeIf { it.isNotBlank() },
                                onClick = onShopping,
                            )
                        }
                    }
                }
            }
            item {
                SectionTitle(title = stringResource(Res.string.ui_a_moment_for_yourself))
                GroupedSurface {
                    NavigationRow(
                        title = stringResource(Res.string.ui_visit_the_reading_corner),
                        description =
                            stringResource(
                                Res.string.ui_fresh_reads_from_the_places_you_love_put_the,
                            ),
                        onClick = onFeed,
                    )
                }
            }
        }
    }
}
