package com.eyther.lumbridge.features.home.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain
import com.eyther.lumbridge.features.home.generated.resources.*
import com.eyther.lumbridge.features.home.presentation.localization.localizedMessage
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    vm: HomeScreenViewModelContract,
    context: HouseholdContextDomain,
    onShopping: () -> Unit,
    onReminders: () -> Unit,
    onMoney: () -> Unit,
    onFeed: () -> Unit,
) {
    val state by vm.state.platformCollectAsStateOnStart()
    InnerHomeScreen(
        overview = state.overview,
        error = state.error,
        context = context,
        onShopping = onShopping,
        onReminders = onReminders,
        onMoney = onMoney,
        onFeed = onFeed,
        onDismiss = vm::dismiss,
    )
}

@Composable
private fun InnerHomeScreen(
    overview: HomeOverviewDomain?,
    error: String?,
    context: HouseholdContextDomain,
    onShopping: () -> Unit,
    onReminders: () -> Unit,
    onMoney: () -> Unit,
    onFeed: () -> Unit,
    onDismiss: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 22.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            PageHeading(
                stringResource(Res.string.ui_a_little_more_together),
                stringResource(Res.string.ui_home_in_harmony),
                stringResource(Res.string.ui_less_to_keep_in_your_head_more_space_for),
            )
        }

        item { ErrorNotice(localizedMessage(error), onDismiss) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    Modifier.weight(1f).clickable(onClick = onShopping),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        LoomIcon(
                            Glyph.SHOPPING,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            pluralStringResource(
                                Res.plurals.shopping_item_count,
                                overview?.shopping?.size ?: 0,
                                overview?.shopping?.size ?: 0,
                            ),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            stringResource(Res.string.ui_on_the_shopping_list),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Surface(
                    Modifier.weight(1f).clickable(onClick = onMoney),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Column(
                        Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        LoomIcon(Glyph.MONEY, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        val balance = overview?.balanceMinor ?: 0
                        Text(
                            MoneyDomain(if (balance < 0) -balance else balance, context.currency)
                                .formatted(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            if (balance >= 0) stringResource(Res.string.ui_you_re_owed)
                            else stringResource(Res.string.ui_you_owe),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
        }
        item {
            SectionTitle(
                stringResource(Res.string.ui_a_gentle_reminder),
                stringResource(Res.string.ui_see_all),
                onReminders,
            )
        }
        if (overview?.reminders?.isEmpty() == true)
            item {
                EmptyNotice(
                    stringResource(Res.string.ui_a_clear_calendar),
                    stringResource(Res.string.ui_enjoy_a_little_breathing_room),
                )
            }
        overview?.reminders?.take(2)?.forEach { reminder ->
            item {
                LoomCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(42.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    RoundedCornerShape(14.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            LoomIcon(Glyph.BELL, tint = MaterialTheme.colorScheme.secondary)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                reminder.dueLabel(context.timeZone),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(onClick = onReminders) { LoomIcon(Glyph.ARROW) }
                    }
                }
            }
        }
        item {
            SectionTitle(
                stringResource(Res.string.ui_pick_up_a_few_things),
                stringResource(Res.string.ui_open_list),
                onShopping,
            )
        }
        item {
            LoomCard {
                overview?.shopping?.take(3)?.forEachIndexed { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(20.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(6.dp),
                                ),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(item.name, Modifier.weight(1f))
                        Text(
                            item.quantity,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (index < minOf(2, overview.shopping.lastIndex))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
        item {
            LoomCard {
                Text(
                    stringResource(Res.string.ui_a_moment_for_yourself),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    stringResource(Res.string.ui_fresh_reads_from_the_places_you_love_put_the),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onFeed) {
                    Text(stringResource(Res.string.ui_visit_the_reading_corner))
                    Spacer(Modifier.width(8.dp))
                    LoomIcon(Glyph.ARROW, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            Text(
                stringResource(Res.string.ui_woven_together_one_little_thing_at_a_time),
                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
