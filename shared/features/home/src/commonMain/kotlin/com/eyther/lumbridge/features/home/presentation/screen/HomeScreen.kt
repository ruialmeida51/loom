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
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import kotlinx.coroutines.flow.*

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
                "A LITTLE MORE TOGETHER",
                "Home, in harmony.",
                "Less to keep in your head. More space for your people.",
            )
        }
        if (context.demo)
            item {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        "SAMPLE HOUSEHOLD · Explore freely",
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        item { ErrorNotice(error, onDismiss) }
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
                            "${overview?.shopping?.size?:0} items",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            "On the shopping list",
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
                            if (balance >= 0) "You’re owed" else "You owe",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
        }
        item { SectionTitle("A gentle reminder", "See all", onReminders) }
        if (overview?.reminders?.isEmpty() == true)
            item { EmptyNotice("A clear calendar.", "Enjoy a little breathing room.") }
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
        item { SectionTitle("Pick up a few things", "Open list", onShopping) }
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
                Text("A moment for yourself", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Fresh reads from the places you love. Put the kettle on.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onFeed) {
                    Text("Visit the reading corner")
                    Spacer(Modifier.width(8.dp))
                    LoomIcon(Glyph.ARROW, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item {
            Text(
                "Woven together, one little thing at a time.",
                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
