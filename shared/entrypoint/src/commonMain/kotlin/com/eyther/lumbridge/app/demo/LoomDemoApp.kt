package com.eyther.lumbridge.app.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.app.demo.data.*
import com.eyther.lumbridge.app.di.householdFeaturesModule
import com.eyther.lumbridge.app.navigation.HouseholdHost
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.feed.domain.datasource.FeedDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.insights.domain.datasource.StatementDataSource
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.shopping.domain.datasource.ShoppingDataSource
import com.eyther.lumbridge.shared.entrypoint.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Explicit development/test entry point. Production LoomApp never selects this graph. */
@Composable
fun LoomDemoApp() {
    val stores = remember {
        DemoHouseholdStore.households.associate { it.id to DemoHouseholdStore(it) }
    }
    var selectedId by remember { mutableStateOf(DemoHouseholdStore.households.first().id) }
    var choosing by remember { mutableStateOf(false) }
    var started by remember { mutableStateOf(true) }
    val store = stores.getValue(selectedId)
    val theme by ThemePreferences.mode.collectAsState()
    LoomTheme(ThemeMode.valueOf(theme)) {
        Surface {
            Column {
                Text(
                    stringResource(Res.string.ui_loom_preview_sample_data),
                    Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                if (started)
                    Box(Modifier.weight(1f)) {
                        key(selectedId) {
                            val graph =
                                remember(store) {
                                    koinApplication {
                                        modules(
                                            householdFeaturesModule(),
                                            module {
                                                single { store.context }
                                                single<HouseholdDataSource> {
                                                    DemoHouseholdDataSource(store)
                                                }
                                                single<ShoppingDataSource> {
                                                    DemoShoppingDataSource(store)
                                                }
                                                single<ReminderDataSource> {
                                                    DemoReminderDataSource(store)
                                                }
                                                single<ExpenseDataSource> {
                                                    DemoExpenseDataSource(store)
                                                }
                                                single<RecurringExpenseDataSource> {
                                                    DemoRecurringExpenseDataSource(store)
                                                }
                                                single<FeedDataSource> { DemoFeedDataSource(store) }
                                                single<SettingsDataSource> {
                                                    DemoSettingsDataSource()
                                                }
                                                single<StatementDataSource> {
                                                    DemoStatementDataSource()
                                                }
                                            },
                                        )
                                    }
                                }
                            DisposableEffect(graph) { onDispose { graph.close() } }
                            KoinIsolatedContext(context = graph) {
                                HouseholdHost(
                                    store.context,
                                    onSwitchHousehold = { choosing = true },
                                    onSignOut = { started = false },
                                )
                            }
                        }
                    }
                else LoomButton(stringResource(Res.string.ui_open_preview), { started = true })
            }
            if (choosing)
                AlertDialog(
                    onDismissRequest = { choosing = false },
                    title = { Text(stringResource(Res.string.ui_choose_a_preview_household)) },
                    text = {
                        Column {
                            DemoHouseholdStore.households.forEach { household ->
                                TextButton(
                                    onClick = {
                                        selectedId = household.id
                                        choosing = false
                                    },
                                ) {
                                    Text(household.name)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { choosing = false }) {
                            Text(stringResource(Res.string.ui_done))
                        }
                    },
                )
        }
    }
}
