package com.eyther.lumbridge.app.demo.data

import androidx.compose.runtime.getValue
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.nowIso
import com.eyther.lumbridge.core.domain.today
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
        val households
            get() = listOf(
                HouseholdContextDomain(
                    "00000000-0000-4000-8000-000000000010",
                    USER,
                    demoText("The little nest", "O ninho"),
                    "EUR",
                    "Europe/Lisbon",
                ),
                HouseholdContextDomain(
                    "00000000-0000-4000-8000-000000000011",
                    USER,
                    demoText("The city flat", "O apartamento da cidade"),
                    "GBP",
                    "Europe/London",
                ),
            )
    }

    val deleted = MutableStateFlow(false)
    val picturePath = MutableStateFlow<String?>(null)

    var members =
        listOf(
            MemberDomain(USER, demoText("You", "Tu"), "owner"),
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
                    name = demoText("Porto weekend", "Fim de semana no Porto"),
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
                listOf(ShoppingListDomain(listId, demoText("Groceries", "Supermercado"), version = 1)),
                listOf(
                    ShoppingItemDomain(
                        newId(),
                        demoText("Avocados", "Abacates"),
                        demoText("3 ripe ones", "3 maduros"),
                        "Produce",
                        false,
                        listId,
                        1,
                    ),
                    ShoppingItemDomain(
                        newId(),
                        demoText("Cherry tomatoes", "Tomate cereja"),
                        demoText("1 punnet", "1 caixa"),
                        "Produce",
                        false,
                        listId,
                        1,
                    ),
                    ShoppingItemDomain(
                        newId(),
                        demoText("Sourdough bread", "Pão de massa-mãe"),
                        demoText("1 loaf", "1 pão"),
                        "Bakery",
                        false,
                        listId,
                        1,
                    ),
                    ShoppingItemDomain(
                        newId(),
                        demoText("Oat milk", "Bebida de aveia"),
                        demoText("2 cartons", "2 pacotes"),
                        "Dairy & eggs",
                        false,
                        listId,
                        1,
                    ),
                    ShoppingItemDomain(
                        newId(),
                        demoText("Free-range eggs", "Ovos do campo"),
                        demoText("6 eggs", "6 ovos"),
                        "Dairy & eggs",
                        false,
                        listId,
                        1,
                    ),
                    ShoppingItemDomain(
                        newId(),
                        demoText("Coffee beans", "Café em grão"),
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
                    demoText("The weekly shop", "As compras da semana"),
                    8645,
                    USER,
                    "Groceries",
                    today(context.timeZone),
                    ExpenseSplitDomain.equal(8645, memberIds),
                    createdAt = (Clock.System.now() - 3.hours).toString(),
                ),
                ExpenseDomain(
                    newId(),
                    "Internet",
                    3990,
                    ALEX,
                    "Bills",
                    today(context.timeZone),
                    ExpenseSplitDomain.equal(3990, memberIds),
                    accountId = context.id,
                    splitMethod = ExpenseSplitMethodDomain.EQUAL,
                    splitValues = emptyMap(),
                    recurring = true,
                    createdAt = (Clock.System.now() - 2.hours).toString(),
                ),
                ExpenseDomain(
                    newId(),
                    demoText("Friday night dinner", "Jantar de sexta-feira"),
                    5400,
                    SAM,
                    "Food & drink",
                    today(context.timeZone),
                    ExpenseSplitDomain.equal(5400, memberIds),
                    createdAt = (Clock.System.now() - 1.hours).toString(),
                ),
                ExpenseDomain(
                    newId(),
                    demoText("Guesthouse", "Alojamento"),
                    18000,
                    USER,
                    "Other",
                    today(context.timeZone),
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
                    MonthlyRecurrenceDomain.nextAfter(today, today.day),
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
                    title = demoText("Water the plants", "Regar as plantas"),
                    dueAt = Clock.System.now() + 2.hours,
                    recurrence = TaskRecurrenceDomain.DAILY,
                    completed = false,
                    notificationEnabled = true,
                ),
                TaskDomain(
                    id = newId(),
                    title = demoText("Take the recycling out", "Levar a reciclagem"),
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
                    title = demoText("Book the boiler service", "Marcar a revisão do esquentador"),
                    dueAt = Clock.System.now() + 30.hours,
                    recurrence = TaskRecurrenceDomain.NONE,
                    completed = false,
                ),
                TaskDomain(
                    id = newId(),
                    title = demoText("Fix the kitchen shelf", "Arranjar a prateleira da cozinha"),
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
                    publisher = demoText("The weekend edit", "Edição de fim de semana"),
                    lastFetchedAt = nowIso(),
                    status = FeedSubscriptionStatusDomain.READY,
                ),
                FeedSubscriptionDomain(
                    id = "table",
                    url = "https://feeds.bbci.co.uk/news/world/rss.xml",
                    publisher = demoText("Around the table", "À volta da mesa"),
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
                    demoText("A slower Sunday starts at home", "Um domingo mais calmo começa em casa"),
                    demoText("Small rituals for a weekend that feels like your own.", "Pequenos rituais para um fim de semana que seja mesmo teu."),
                    "https://www.theguardian.com/lifeandstyle",
                    demoText("The weekend edit", "Edição de fim de semana"),
                    Clock.System.now(),
                    false,
                    subscriptionId = "weekend",
                    imageUrl = DEMO_NEWS_PICTURES + "news-sunday.jpg",
                ),
                ArticleDomain(
                    newId(),
                    demoText("Good food, less waste", "Boa comida, menos desperdício"),
                    demoText("Making the most of what is already in your fridge.", "Tirar o melhor partido do que já tens no frigorífico."),
                    "https://www.bbcgoodfood.com/",
                    demoText("Around the table", "À volta da mesa"),
                    Clock.System.now(),
                    false,
                    subscriptionId = "table",
                    imageUrl = DEMO_NEWS_PICTURES + "news-food.jpg",
                ),
                ArticleDomain(
                    newId(),
                    demoText("Rents steady across the city this autumn", "Rendas estáveis na cidade neste outono"),
                    demoText("What the latest figures mean if your lease is up for renewal.", "O que os números mais recentes significam se o teu contrato está para renovar."),
                    "https://www.theguardian.com/money",
                    demoText("The weekend edit", "Edição de fim de semana"),
                    Clock.System.now() - 3.hours,
                    false,
                    subscriptionId = "weekend",
                    imageUrl = DEMO_NEWS_PICTURES + "news-city.jpg",
                ),
            ),
        )
}
