package com.eyther.lumbridge.app.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.app.demo.data.DemoAnalyticsDataSource
import com.eyther.lumbridge.app.demo.data.DemoExpenseDataSource
import com.eyther.lumbridge.app.demo.data.DemoFeedDataSource
import com.eyther.lumbridge.app.demo.data.DemoHouseholdDataSource
import com.eyther.lumbridge.app.demo.data.DemoHouseholdStore
import com.eyther.lumbridge.app.demo.data.DemoRecurringExpenseDataSource
import com.eyther.lumbridge.app.demo.data.DemoReminderDataSource
import com.eyther.lumbridge.app.demo.data.DemoSettingsDataSource
import com.eyther.lumbridge.app.demo.data.DemoShoppingDataSource
import com.eyther.lumbridge.app.demo.data.DemoStatementDataSource
import com.eyther.lumbridge.app.navigation.HouseholdHost
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.di.providedHouseholdModules
import com.eyther.lumbridge.features.insights.data.export.CommonStatementExportDataSource
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_choose_a_preview_household
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_done
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_loom_preview_sample_data
import com.eyther.lumbridge.shared.entrypoint.generated.resources.ui_open_preview
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinIsolatedContext
import org.koin.dsl.koinApplication

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
                                            providedHouseholdModules(
                                                context = store.context,
                                                householdDataSource = DemoHouseholdDataSource(store = store),
                                                analyticsDataSource = DemoAnalyticsDataSource(),
                                                shoppingDataSource = DemoShoppingDataSource(store = store),
                                                reminderDataSource = DemoReminderDataSource(store = store),
                                                expenseDataSource = DemoExpenseDataSource(store = store),
                                                recurringExpenseDataSource = DemoRecurringExpenseDataSource(store = store),
                                                feedDataSource = DemoFeedDataSource(store = store),
                                                statementDataSource = DemoStatementDataSource(),
                                                statementExportDataSource = CommonStatementExportDataSource(),
                                                settingsDataSource = DemoSettingsDataSource(),
                                            ),
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
