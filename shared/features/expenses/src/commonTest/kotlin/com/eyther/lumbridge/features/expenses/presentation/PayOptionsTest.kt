package com.eyther.lumbridge.features.expenses.presentation

import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.expenses.data.fixtures.PaymentWorkflowFixture
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.PaymentFormState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.collections.immutable.toImmutableList

class PayOptionsTest {
    private val fixture = PaymentWorkflowFixture()
    private val alex =
        PaymentHandlesDomain("alex", mbwayPhone = "+351912345678", paypalUsername = "AlexS")
    private val state =
        PaymentFormState(
            userId = "you",
            account = fixture.references.account,
            members = fixture.references.members.toImmutableList(),
            from = "you",
            to = "alex",
            amount = "12,5",
            loading = false,
            paymentHandles = mapOf("alex" to alex),
        )

    @Test
    fun theRecipientsDetailsOfferWaysToPayWithTheAmountFilledIn() {
        val options = requireNotNull(state.payOptions)
        assertEquals("Alex", options.recipientName)
        assertEquals("12.50", options.amount)
        assertEquals("https://paypal.me/AlexS/12.50EUR", options.paypalLink)
        assertEquals("+351912345678", options.handles.mbwayPhone)
    }

    @Test
    fun waysToPayOnlyShowWhenYouArePayingSomeoneWhoSharesThem() {
        assertNull(state.copy(from = "alex", to = "you").payOptions)
        assertNull(state.copy(paymentHandles = emptyMap()).payOptions)
        assertNull(state.copy(amount = "").payOptions?.paypalLink)
    }
}
