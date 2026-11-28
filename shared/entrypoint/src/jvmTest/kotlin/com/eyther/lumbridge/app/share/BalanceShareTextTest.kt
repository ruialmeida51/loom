package com.eyther.lumbridge.app.share

import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountIconDomain
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.mapper.balanceShareText
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.test.runTest

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
}
