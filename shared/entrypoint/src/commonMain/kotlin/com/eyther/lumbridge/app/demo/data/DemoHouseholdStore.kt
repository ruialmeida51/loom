package com.eyther.lumbridge.app.demo.data

import androidx.compose.runtime.getValue
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.nowIso
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.MonthlyRecurrenceDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate

/** Preview data is owned by this package and never selected by production data sources. */
class DemoHouseholdStore(val context: HouseholdContextDomain) {

    companion object {
        const val USER = "00000000-0000-4000-8000-000000000001"
        const val ALEX = "00000000-0000-4000-8000-000000000002"
        const val SAM = "00000000-0000-4000-8000-000000000003"
        val households =
            listOf(
                HouseholdContextDomain(
                    "00000000-0000-4000-8000-000000000010",
                    USER,
                    "The little nest",
                    "EUR",
                    "Europe/Lisbon",
                ),
                HouseholdContextDomain(
                    "00000000-0000-4000-8000-000000000011",
                    USER,
                    "The city flat",
                    "GBP",
                    "Europe/London",
                ),
            )
    }

    var members =
        listOf(
            MemberDomain(USER, "You", "owner"),
            MemberDomain(ALEX, "Alex", "member"),
            MemberDomain(SAM, "Sam", "member"),
        )
    val memberUpdates = MutableStateFlow(members)
    val selectedExpenseAccount = MutableStateFlow(context.id)
    val selectedShoppingList = MutableStateFlow<String?>(null)
    val expenseAccounts =
        MutableStateFlow(
            listOf(
                ExpenseAccountDomain(
                    id = context.id,
                    name = "Everyday",
                    currency = context.currency,
                    memberIds = members.map { it.id }.toSet(),
                    isDefault = true,
                    version = 1,
                    createdBy = context.userId,
                ),
            ),
        )

    private val listId = newId()
    val shopping =
        MutableStateFlow(
            ShoppingSnapshotDomain(
                listOf(ShoppingListDomain(listId, "Groceries", version = 1)),
                listOf(
                    ShoppingItemDomain(
                        newId(),
                        "Avocados",
                        "3 ripe ones",
                        "Produce",
                        false,
                        listId,
                        1,
                    ),
                    ShoppingItemDomain(
                        newId(),
                        "Cherry tomatoes",
                        "1 punnet",
                        "Produce",
                        false,
                        listId,
                        1,
                    ),
                    ShoppingItemDomain(
                        newId(),
                        "Sourdough bread",
                        "1 loaf",
                        "Bakery",
                        false,
                        listId,
                        1,
                    ),
                    ShoppingItemDomain(
                        newId(),
                        "Oat milk",
                        "2 cartons",
                        "Dairy & eggs",
                        false,
                        listId,
                        1,
                    ),
                    ShoppingItemDomain(
                        newId(),
                        "Free-range eggs",
                        "6 eggs",
                        "Dairy & eggs",
                        false,
                        listId,
                        1,
                    ),
                    ShoppingItemDomain(
                        newId(),
                        "Coffee beans",
                        "250 g",
                        "Pantry",
                        true,
                        listId,
                        1,
                        favorite = true,
                    ),
                ),
            ),
        )
    private val memberIds = members.map { it.id }
    val expenses =
        MutableStateFlow(
            listOf(
                ExpenseDomain(
                    newId(),
                    "The weekly shop",
                    8645,
                    USER,
                    "Groceries",
                    todayIso(context.timeZone),
                    ExpenseSplitDomain.equal(8645, memberIds),
                ),
                ExpenseDomain(
                    newId(),
                    "Internet",
                    3990,
                    ALEX,
                    "Bills",
                    todayIso(context.timeZone),
                    ExpenseSplitDomain.equal(3990, memberIds),
                    true,
                ),
                ExpenseDomain(
                    newId(),
                    "Friday night dinner",
                    5400,
                    SAM,
                    "Food & drink",
                    todayIso(context.timeZone),
                    ExpenseSplitDomain.equal(5400, memberIds),
                ),
            ),
        )
    val settlements = MutableStateFlow<List<SettlementDomain>>(emptyList())
    private val today = LocalDate.parse(todayIso(context.timeZone))
    val recurring =
        MutableStateFlow(
            listOf(
                RecurringExpenseDomain(
                    newId(),
                    "Internet",
                    3990,
                    ALEX,
                    "Bills",
                    MonthlyRecurrenceDomain.nextAfter(today, today.day).toString(),
                    today.day,
                    true,
                    false,
                    USER,
                    1,
                    ExpenseSplitDomain.equal(3990, memberIds),
                ),
            ),
        )
    val reminders =
        MutableStateFlow(
            listOf(
                ReminderDomain(
                    newId(),
                    "Water the plants",
                    (Clock.System.now() + 2.hours).toString(),
                    "weekly",
                    false,
                ),
                ReminderDomain(
                    newId(),
                    "Take the recycling out",
                    (Clock.System.now() + 7.hours).toString(),
                    "weekly",
                    false,
                ),
                ReminderDomain(
                    newId(),
                    "Book the boiler service",
                    (Clock.System.now() + 30.hours).toString(),
                    "none",
                    false,
                ),
            ),
        )
    val articles =
        MutableStateFlow(
            listOf(
                ArticleDomain(
                    newId(),
                    "A slower Sunday starts at home",
                    "Small rituals for a weekend that feels like your own.",
                    "https://www.theguardian.com/lifeandstyle",
                    "The weekend edit",
                    nowIso(),
                    false,
                ),
                ArticleDomain(
                    newId(),
                    "Good food, less waste",
                    "Making the most of what is already in your fridge.",
                    "https://www.bbcgoodfood.com/",
                    "Around the table",
                    nowIso(),
                    false,
                ),
            ),
        )
}
