package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import com.eyther.lumbridge.app.demo.DemoPortalHost
import com.eyther.lumbridge.app.demo.data.DemoHouseholdStore
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

@OptIn(ExperimentalTestApi::class)
class HouseholdPolishUiTest {
    @Test
    fun reimbursementsCanBeCorrectedAndDeletedAndPurchasesHaveAnchoredMenus() =
        runSkikoComposeUiTest(size = Size(390f, 820f), density = Density(1f, 1.2f)) {
            val store = DemoHouseholdStore(DemoHouseholdStore.households.first())
            store.settlements.value =
                listOf(
                    SettlementDomain(
                        id = "payment",
                        from = DemoHouseholdStore.ALEX,
                        to = DemoHouseholdStore.USER,
                        amountMinor = 1250,
                        occurredOn = todayIso(store.context.timeZone),
                        accountId = store.context.id,
                        createdBy = DemoHouseholdStore.ALEX,
                        version = 1,
                    ),
                )
            setContent {
                LoomTheme(ThemeMode.LIGHT) {
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
                .performScrollToNode(hasTestTag("payment_actions_payment"))
            onNodeWithTag("payment_actions_payment").performClick()
            capture("payment-card-menu-light")
            onNodeWithText("Edit").performClick()
            onNodeWithText("Edit payment").assertIsDisplayed()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("repayment_amount").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("repayment_amount").performTextClearance()
            onNodeWithTag("repayment_amount").performTextInput("20.00")
            capture("payment-edit-light-narrow")
            onNodeWithTag("record_repayment_submit").performClick()
            waitUntil(timeoutMillis = 5_000) {
                store.settlements.value.single().amountMinor == 2000L
            }
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("record_repayment_submit").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("payment_actions_payment").performClick()
            onNodeWithText("Delete").performClick()
            onNodeWithTag("confirm_delete_payment").assertIsDisplayed()
            onNodeWithText("Cancel").performClick()
            assertEquals(1, store.settlements.value.size)
            onNodeWithTag("payment_actions_payment").performClick()
            onNodeWithText("Delete").performClick()
            onNodeWithTag("confirm_delete_payment").performClick()
            waitUntil(timeoutMillis = 5_000) { store.settlements.value.isEmpty() }
            val expense = store.expenses.value.first()
            onNodeWithTag("expenses_content")
                .performScrollToNode(hasTestTag("expense_actions_${expense.id}"))
            onNodeWithTag("expense_actions_${expense.id}").performClick()
            onNodeWithText("Edit").assertIsDisplayed()
            onNodeWithText("Delete").performClick()
            onNodeWithText("Cancel").performClick()
            assertTrue(store.expenses.value.any { it.id == expense.id })
        }

    @Test
    fun moreUsesIconsAndPrivacyChoicesRemainReadableWithStackedActions() =
        runSkikoComposeUiTest(size = Size(360f, 800f), density = Density(1f, 1.3f)) {
            val previous = PlatformStorage.get("usage-data-consent-v1")
            val store = DemoHouseholdStore(DemoHouseholdStore.households.first())
            try {
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
                onNodeWithTag("navigation_more").performClick()
                onNodeWithTag("more_content").assertIsDisplayed()
                capture("more-icons-dark")
                onNodeWithTag("open_my_account").performClick()
                awaitLoadedDestination("account_content")
                onNodeWithTag("account_content")
                    .performScrollToNode(hasTestTag("usage_data_settings"))
                onNodeWithTag("usage_data_settings").performClick()
                waitUntil(timeoutMillis = 5_000) {
                    onAllNodesWithTag("usage_data_consent").fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithTag("consent_allow_all").assertIsDisplayed()
                onNodeWithTag("consent_save").assertIsDisplayed()
                val allow = onNodeWithTag("consent_allow_all").fetchSemanticsNode().boundsInWindow
                val save = onNodeWithTag("consent_save").fetchSemanticsNode().boundsInWindow
                assertTrue(save.top >= allow.bottom, "Consent actions must stack vertically")
                capture("privacy-choices-dark-large-text")
                onNodeWithTag("consent_save").performClick()
                waitUntil(timeoutMillis = 5_000) {
                    onAllNodesWithTag("usage_data_consent").fetchSemanticsNodes().isEmpty()
                }
                onNodeWithTag("account_content").assertIsDisplayed()
            } finally {
                PlatformStorage.put("usage-data-consent-v1", previous)
            }
        }

    private fun SkikoComposeUiTest.capture(name: String) {
        waitForIdle()
        val folder =
            File(System.getProperty("loom.screenshots.dir", "build/screenshots")).apply { mkdirs() }
        File(folder, "$name.png")
            .writeBytes(
                requireNotNull(
                        Image.makeFromBitmap(captureToImage().asSkiaBitmap())
                            .encodeToData(EncodedImageFormat.PNG),
                    )
                    .bytes,
            )
    }
}
