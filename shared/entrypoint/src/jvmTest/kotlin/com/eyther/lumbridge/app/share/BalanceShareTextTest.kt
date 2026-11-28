package com.eyther.lumbridge.app.share

import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.expenses.domain.model.AccountSpendingDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountIconDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.mapper.balanceShareText
import com.eyther.lumbridge.features.expenses.presentation.mapper.expenseShareText
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseDetailsUi
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseShareUi
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class BalanceShareTextTest {
    private val original = Locale.getDefault()
    private val trip =
        ExpenseAccountDomain(
            id = "trip",
            name = "Porto",
            currency = "EUR",
            memberIds = setOf("rui", "alex", "sam"),
            icon = ExpenseAccountIconDomain.TRAVEL,
        )
    private val names = mapOf("rui" to "Rui", "alex" to "Alex", "sam" to "Sam")
    private val handles =
        PaymentHandlesDomain(
            userId = "rui",
            mbwayPhone = "+351912345678",
            revolutUsername = "rui",
            paypalUsername = "RuiA",
        )

    @BeforeTest fun english() = Locale.setDefault(Locale.UK)

    @AfterTest fun restore() = Locale.setDefault(original)

    @Test
    fun balancesNameEveryoneAndSayHowToPayWhoeverIsOwed() = runTest {
        val text =
            balanceShareText(
                account = trip,
                names = names,
                balances = listOf("rui" to 2040L, "alex" to -1240L, "sam" to -800L),
                suggestions =
                    listOf(
                        SuggestedSettlementDomain(from = "alex", to = "rui", amountMinor = 1240),
                        SuggestedSettlementDomain(from = "sam", to = "rui", amountMinor = 800),
                    ),
                userId = "rui",
                ownHandles = handles,
            )
        assertEquals(
            """
            ✈️ Porto · Balances
            ━━━━━━━━━━━━
            To settle up
            • Alex → Rui: €12.40
            • Sam → Rui: €8.00

            Where everyone stands
            • Rui is owed €20.40
            • Alex owes €12.40
            • Sam owes €8.00

            How to pay me
            MB Way: +351 912 345 678
            Revolut: revolut.me/rui
            PayPal: paypal.me/RuiA

            Shared from Loom
            """
                .trimIndent(),
            text,
        )
    }

    @Test
    fun aSettledAccountSaysSoWithoutPaymentDetails() = runTest {
        val text =
            balanceShareText(
                account = trip,
                names = names,
                balances = listOf("rui" to 0L, "alex" to 0L),
                suggestions = emptyList(),
                userId = "rui",
                ownHandles = handles,
            )
        assertEquals("✈️ Porto · Balances\n━━━━━━━━━━━━\nAll settled up ✅\n\nShared from Loom", text)
    }

    @Test
    fun paymentDetailsStayOutWhenYouAreTheOneWhoOwes() = runTest {
        val text =
            balanceShareText(
                account = trip,
                names = names,
                balances = listOf("rui" to -500L, "alex" to 500L),
                suggestions =
                    listOf(SuggestedSettlementDomain(from = "rui", to = "alex", amountMinor = 500)),
                userId = "rui",
                ownHandles = handles,
            )
        assertFalse("How to pay me" in text)
    }

    @Test
    fun aTripSummarySaysWhatWasSpentAndWhoPaidBeforeSettlingUp() = runTest {
        val text =
            balanceShareText(
                account = trip,
                names = names,
                balances = listOf("rui" to 500L, "alex" to -500L),
                suggestions =
                    listOf(SuggestedSettlementDomain(from = "alex", to = "rui", amountMinor = 500)),
                userId = "alex",
                ownHandles = null,
                spending =
                    AccountSpendingDomain(
                        paidByPerson = mapOf("alex" to 3000L, "rui" to 16000L),
                        expenseCount = 3,
                    ),
            )
        assertEquals(
            """
            ✈️ Porto · Summary
            ━━━━━━━━━━━━
            Spent €190.00 on 3 expenses
            Who paid
            • Rui €160.00
            • Alex €30.00

            To settle up
            • Alex → Rui: €5.00

            Where everyone stands
            • Rui is owed €5.00
            • Alex owes €5.00

            Shared from Loom
            """
                .trimIndent(),
            text,
        )
    }

    @Test
    fun oneExpenseReadsWithItsSplit() = runTest {
        val text =
            expenseShareText(
                details =
                    ExpenseDetailsUi(
                        title = "Dinner at Taberna",
                        amount = "€48.00",
                        category = "Food",
                        occurredOn = LocalDate(2026, 11, 22),
                        payerName = "Alex",
                        accountName = "Porto",
                        isEveryday = false,
                        recurring = false,
                        splitMethod = ExpenseSplitMethodDomain.EQUAL,
                        ownShare = "€16.00",
                        shares =
                            persistentListOf(
                                ExpenseShareUi("rui", "Rui", "€16.00", isCurrentUser = true, isPayer = false),
                                ExpenseShareUi("sam", null, "€16.00", isCurrentUser = false, isPayer = false),
                                ExpenseShareUi("alex", "Alex", "€16.00", isCurrentUser = false, isPayer = true),
                            ),
                    ),
                date = "22 November 2026",
            )
        assertEquals(
            """
            🧾 Dinner at Taberna
            ━━━━━━━━━━━━
            €48.00 · 22 November 2026
            Paid by Alex · Porto

            Split
            • Rui €16.00
            • Former member €16.00
            • Alex €16.00

            Shared from Loom
            """
                .trimIndent(),
            text,
        )
    }
}
