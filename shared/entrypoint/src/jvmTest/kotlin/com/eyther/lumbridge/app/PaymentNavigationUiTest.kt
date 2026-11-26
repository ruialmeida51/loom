package com.eyther.lumbridge.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.NavigationEventInput
import androidx.navigationevent.OnBackCompletedFallback
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.eyther.lumbridge.app.demo.DemoPortalHost
import com.eyther.lumbridge.app.demo.data.DemoHouseholdStore
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.app.testutil.capture
import com.eyther.lumbridge.core.domain.today
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class PaymentNavigationUiTest {
    @Test
    fun paymentDetailsEditAndDeleteMatchTheExpenseFlowAndDateBackKeepsTheDraft() =
        runSkikoComposeUiTest(size = Size(390f, 820f), density = Density(1f, 1.2f)) {
            val store = paymentStore()
            var exits = 0
            val dispatcher =
                NavigationEventDispatcher(
                    object : OnBackCompletedFallback {
                        override fun onBackCompletedFallback() {
                            exits++
                        }
                    },
                )
            val input = BackInput()
            dispatcher.addInput(input)
            val owner =
                object : NavigationEventDispatcherOwner {
                    override val navigationEventDispatcher = dispatcher
                }
            setContent {
                CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner) {
                    LoomTheme(ThemeMode.LIGHT) {
                        DemoPortalHost(
                            stores = mapOf(store.context.id to store),
                            selectedId = store.context.id,
                            onSelect = {},
                            onSignOut = {},
                        )
                    }
                }
            }
            awaitLoadedDestination("home_content")
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination("expenses_content")
            onNodeWithTag("expenses_content").performScrollToNode(hasText("Alex paid me"))
            onNodeWithText("Alex paid me").performClick()
            awaitLoadedDestination("payment_details_content")
            onNodeWithText("€12.50").assertIsDisplayed()
            onNodeWithTag("modal_sheet").assertDoesNotExist()
            capture("payment-details-light")
            onNodeWithTag("payment_details_actions").performClick()
            onNodeWithText("Edit").assertExists()
            onNodeWithText("Edit").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("repayment_amount").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("repayment_amount").performTextReplacement("20.00")
            onNodeWithTag("repayment_date").performClick()
            onNodeWithTag("form_sheet").assertIsDisplayed()
            runOnIdle { input.back() }
            onNodeWithTag("repayment_amount").assertTextContains("20.00")
            assertEquals(0, exits)
            onNodeWithTag("record_repayment_submit").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("payment_form_content").fetchSemanticsNodes().isEmpty() &&
                    onAllNodesWithText("€20.00").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("payment_details_content").assertIsDisplayed()
            assertEquals(2000, store.settlements.value.single().amountMinor)
            onNodeWithTag("payment_details_actions").performClick()
            onNodeWithText("Delete").performClick()
            onNodeWithText("Cancel").performClick()
            assertEquals(1, store.settlements.value.size)
            onNodeWithTag("payment_details_actions").performClick()
            onNodeWithText("Delete").performClick()
            onNodeWithTag("confirm_delete_payment_details").performClick()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("payment_details_content").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("expenses_content").assertIsDisplayed()
            assertEquals(0, store.settlements.value.size)
            assertEquals(0, exits)
        }

    @Test
    fun paymentsRemainReadableWhenTheViewerCannotEditThem() =
        runSkikoComposeUiTest(size = Size(360f, 740f), density = Density(1f, 1.3f)) {
            val store = paymentStore()
            store.members = store.members.map { it.copy(role = "member") }
            store.memberUpdates.value = store.members
            store.settlements.value =
                store.settlements.value.map { it.copy(to = DemoHouseholdStore.SAM) }
            setContent {
                LoomTheme(ThemeMode.DARK) {
                    DemoPortalHost(
                        stores = mapOf(store.context.id to store),
                        selectedId = store.context.id,
                        onSelect = {},
                        onSignOut = {},
                    )
                }
            }
            awaitLoadedDestination("home_content")
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination("expenses_content")
            onNodeWithTag("expenses_content")
                .performScrollToNode(hasTestTag("payment_card_payment"))
            // Tap the leading icon: the floating Add expense control overlaps the list's trailing
            // side.
            onNodeWithTag("payment_card_payment").assertIsEnabled().performTouchInput {
                click(Offset(x = 24f, y = height / 2f))
            }
            awaitLoadedDestination("payment_details_content")
            onNodeWithText("€12.50").assertIsDisplayed()
            onNodeWithTag("payment_details_actions").assertDoesNotExist()
            capture("payment-details-dark-read-only")
        }

    private fun paymentStore(): DemoHouseholdStore =
        DemoHouseholdStore(DemoHouseholdStore.households.first()).apply {
            settlements.value =
                listOf(
                    SettlementDomain(
                        id = "payment",
                        from = DemoHouseholdStore.ALEX,
                        to = DemoHouseholdStore.USER,
                        amountMinor = 1250,
                        occurredOn = today(context.timeZone),
                        accountId = context.id,
                        createdBy = DemoHouseholdStore.ALEX,
                        version = 1,
                    ),
                )
        }

    private class BackInput : NavigationEventInput() {
        fun back() {
            dispatchOnBackCompleted()
        }
    }

}
