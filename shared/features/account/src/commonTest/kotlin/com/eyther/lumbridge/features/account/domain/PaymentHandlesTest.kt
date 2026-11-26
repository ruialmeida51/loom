package com.eyther.lumbridge.features.account.domain

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.datasource.PaymentHandlesDataSource
import com.eyther.lumbridge.features.account.domain.errors.AccountError
import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.account.domain.usecase.SavePaymentHandlesUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class PaymentHandlesTest {
    @Test
    fun whatPeopleTypeOrPasteIsCleanedUp() {
        val clean =
            PaymentHandlesDomain(
                    userId = "you",
                    mbwayPhone = "912 345-678",
                    revolutUsername = " https://revolut.me/@Alex.S/ ",
                    paypalUsername = "paypal.me/AlexS/10EUR",
                )
                .normalized()
        assertEquals("+351912345678", clean.mbwayPhone)
        assertEquals("alex.s", clean.revolutUsername)
        assertEquals("AlexS", clean.paypalUsername)
        assertTrue(clean.isValid())
        assertEquals("+447700900123", PaymentHandlesDomain("you", mbwayPhone = "0044 7700 900123").normalized().mbwayPhone)
        assertNull(PaymentHandlesDomain("you", mbwayPhone = "  ", revolutUsername = "").normalized().mbwayPhone)
        assertTrue(PaymentHandlesDomain("you", mbwayPhone = " ").normalized().isEmpty)
    }

    @Test
    fun linksCarryTheAmountWherePossible() {
        val alex = PaymentHandlesDomain("alex", revolutUsername = "alex.s", paypalUsername = "AlexS")
        assertEquals("https://paypal.me/AlexS/12.50EUR", alex.paypalLink(amount = "12.50", currency = "EUR"))
        assertEquals("https://revolut.me/alex.s", alex.revolutLink())
        assertNull(PaymentHandlesDomain("sam").paypalLink(amount = "1.00", currency = "EUR"))
    }

    @Test
    fun malformedDetailsNeverReachTheServer() = runTest {
        var saves = 0
        val source =
            object : PaymentHandlesDataSource {
                override suspend fun get(userIds: Collection<String>) = AppResult.Success(emptyList<PaymentHandlesDomain>())

                override suspend fun save(handles: PaymentHandlesDomain): AppResult<PaymentHandlesDomain> {
                    saves++
                    return AppResult.Success(handles)
                }
            }
        val save = SavePaymentHandlesUseCase(source)
        assertIs<AccountError.InvalidPaymentHandle>(
            assertIs<AppResult.Failure>(save(PaymentHandlesDomain("you", mbwayPhone = "12345"))).error,
        )
        assertIs<AccountError.InvalidPaymentHandle>(
            assertIs<AppResult.Failure>(save(PaymentHandlesDomain("you", revolutUsername = "has space"))).error,
        )
        assertEquals(0, saves)
        assertIs<AppResult.Success<PaymentHandlesDomain>>(save(PaymentHandlesDomain("you", mbwayPhone = "912345678")))
        assertEquals(1, saves)
    }
}
