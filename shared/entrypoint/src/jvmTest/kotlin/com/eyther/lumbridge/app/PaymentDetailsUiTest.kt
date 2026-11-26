package com.eyther.lumbridge.app

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.eyther.lumbridge.app.demo.LoomDemoApp
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PaymentDetailsUiTest {
    @Test
    fun mbWayNumberIsSavedFromTheAccount() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            setContent { LoomDemoApp() }
            waitUntil(timeoutMillis = 10_000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("open_my_account").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("edit_payment_details").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("edit_payment_details").performScrollTo().performClick()
            onNodeWithTag("payment_mbway").performTextInput("912 345 678")
            onNodeWithTag("save_payment_details").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("1 way to pay you", substring = true).fetchSemanticsNodes().isNotEmpty()
            }
        }

    @Test
    fun aMalformedNumberIsExplained() =
        runSkikoComposeUiTest(size = Size(440f, 900f)) {
            setContent { LoomDemoApp() }
            waitUntil(timeoutMillis = 10_000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("open_my_account").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("edit_payment_details").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("edit_payment_details").performScrollTo().performClick()
            onNodeWithTag("payment_mbway").performTextInput("123")
            onNodeWithTag("save_payment_details").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("country code", substring = true).fetchSemanticsNodes().isNotEmpty()
            }
        }
}
