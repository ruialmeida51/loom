package com.eyther.lumbridge.app.demo.data

import androidx.compose.runtime.getValue
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.nowIso
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.MonthlyRecurrenceDomain
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.feed.domain.model.ArticleDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionDomain
import com.eyther.lumbridge.features.feed.domain.model.FeedSubscriptionStatusDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate

/** Preview data is owned by this package and never selected by production data sources. */
class DemoHouseholdStore(val context: HouseholdContextDomain) {

    companion object {
        const val USER = "00000000-0000-4000-8000-000000000001"
        const val ALEX = "00000000-0000-4000-8000-000000000002"
        const val SAM = "00000000-0000-4000-8000-000000000003"
        const val TRIP = "00000000-0000-4000-8000-000000000101"
        const val MARTA = "00000000-0000-4000-8000-000000000201"
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

    val deleted = MutableStateFlow(false)
    val picturePath = MutableStateFlow<String?>(null)

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
                // A trip shared with Marta, a guest outside the household.
                ExpenseAccountDomain(
                    id = TRIP,
                    name = "Porto weekend",
                    currency = context.currency,
                    memberIds = setOf(USER, ALEX),
                    version = 1,
                    createdBy = context.userId,
                    guests = listOf(ExpenseGuestDomain(id = MARTA, name = "Marta", version = 1)),
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
                )
                    // Like the server, every row has its own position.
                    .mapIndexed { index, item -> item.copy(position = index + 1L) },
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
                    createdAt = (Clock.System.now() - 3.hours).toString(),
                ),
                ExpenseDomain(
                    newId(),
                    "Internet",
                    3990,
                    ALEX,
                    "Bills",
                    todayIso(context.timeZone),
                    ExpenseSplitDomain.equal(3990, memberIds),
                    accountId = context.id,
                    splitMethod = ExpenseSplitMethodDomain.EQUAL,
                    splitValues = emptyMap(),
                    recurring = true,
                    createdAt = (Clock.System.now() - 2.hours).toString(),
                ),
                ExpenseDomain(
                    newId(),
                    "Friday night dinner",
                    5400,
                    SAM,
                    "Food & drink",
                    todayIso(context.timeZone),
                    ExpenseSplitDomain.equal(5400, memberIds),
                    createdAt = (Clock.System.now() - 1.hours).toString(),
                ),
                ExpenseDomain(
                    newId(),
                    "Guesthouse",
                    18000,
                    USER,
                    "Other",
                    todayIso(context.timeZone),
                    ExpenseSplitDomain.equal(18000, listOf(USER, ALEX, MARTA)),
                    accountId = TRIP,
                    splitMethod = ExpenseSplitMethodDomain.EQUAL,
                    splitValues = emptyMap(),
                    createdAt = (Clock.System.now() - 30.minutes).toString(),
                ),
            ),
        )
    val receiptPhotos = mutableMapOf<String, ReceiptPhotoDomain>()
    val expenseReceipts =
        expenses.value.first().let { expense ->
            mutableMapOf(
                expense.id to
                    ExpenseReceiptDomain(
                        expenseId = expense.id,
                        accountId = context.id,
                        objectPath = "demo/${expense.id}/receipt.jpg",
                        version = 1,
                    ),
            )
        }
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
                    accountId = context.id,
                    splitMethod = ExpenseSplitMethodDomain.EQUAL,
                    splitValues = emptyMap(),
                ),
            ),
        )
    val tasks =
        MutableStateFlow(
            listOf(
                TaskDomain(
                    id = newId(),
                    title = "Water the plants",
                    dueAt = Clock.System.now() + 2.hours,
                    recurrence = TaskRecurrenceDomain.DAILY,
                    completed = false,
                    notificationEnabled = true,
                ),
                TaskDomain(
                    id = newId(),
                    title = "Take the recycling out",
                    dueAt = Clock.System.now() + 7.hours,
                    recurrence = TaskRecurrenceDomain.WEEKLY,
                    completed = false,
                    notificationEnabled = true,
                    // Everyone takes turns; it is Alex's week.
                    assigneeId = ALEX,
                    rotation = listOf(USER, ALEX, SAM),
                ),
                TaskDomain(
                    id = newId(),
                    title = "Book the boiler service",
                    dueAt = Clock.System.now() + 30.hours,
                    recurrence = TaskRecurrenceDomain.NONE,
                    completed = false,
                ),
                TaskDomain(
                    id = newId(),
                    title = "Fix the kitchen shelf",
                    dueAt = null,
                    recurrence = TaskRecurrenceDomain.NONE,
                    completed = false,
                ),
            ),
        )
    val feedSubscriptions =
        MutableStateFlow(
            listOf(
                FeedSubscriptionDomain(
                    id = "weekend",
                    url = "https://www.theguardian.com/lifeandstyle/rss",
                    publisher = "The weekend edit",
                    lastFetchedAt = nowIso(),
                    status = FeedSubscriptionStatusDomain.READY,
                ),
                FeedSubscriptionDomain(
                    id = "table",
                    url = "https://feeds.bbci.co.uk/news/world/rss.xml",
                    publisher = "Around the table",
                    lastFetchedAt = nowIso(),
                    status = FeedSubscriptionStatusDomain.READY,
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
                    Clock.System.now(),
                    false,
                    subscriptionId = "weekend",
                    imageUrl = DEMO_NEWS_PICTURES + "news-sunday.jpg",
                ),
                ArticleDomain(
                    newId(),
                    "Good food, less waste",
                    "Making the most of what is already in your fridge.",
                    "https://www.bbcgoodfood.com/",
                    "Around the table",
                    Clock.System.now(),
                    false,
                    subscriptionId = "table",
                    imageUrl = DEMO_NEWS_PICTURES + "news-food.jpg",
                ),
                ArticleDomain(
                    newId(),
                    "Rents steady across the city this autumn",
                    "What the latest figures mean if your lease is up for renewal.",
                    "https://www.theguardian.com/money",
                    "The weekend edit",
                    Clock.System.now() - 3.hours,
                    false,
                    subscriptionId = "weekend",
                    imageUrl = DEMO_NEWS_PICTURES + "news-city.jpg",
                ),
            ),
        )
}
