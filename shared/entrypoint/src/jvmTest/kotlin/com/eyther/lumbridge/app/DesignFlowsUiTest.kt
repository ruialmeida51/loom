package com.eyther.lumbridge.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SkikoComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.home.data.EmptyHomeViewModel
import com.eyther.lumbridge.app.home.data.HomeRetryFixture
import com.eyther.lumbridge.core.presentation.components.toolbar.Toolbar
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.home.presentation.screen.HomeScreen
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.cancel
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

@OptIn(ExperimentalTestApi::class)
class DesignFlowsUiTest {

    @Test
    fun shoppingSheetKeepsItsSaveActionReachableOnANarrowShortViewportWithLargerText() =
        runSkikoComposeUiTest(
            size = Size(360f, 540f),
            density = Density(density = 1f, fontScale = 1.3f),
        ) {
            ThemePreferences.set("LIGHT")
            setContent { Box(Modifier.fillMaxSize().testTag("loom_app")) { LoomDemoApp() } }
            waitForHome()
            onNodeWithTag("navigation_shopping").performClick()
            onNodeWithTag("edit_Avocados").performClick()
            onNodeWithTag("form_sheet").assertIsDisplayed()
            onNodeWithText("What do we need?")
                .performScrollTo()
                .performTextReplacement("Barista oat drink")
            onNodeWithText("Save item").assertIsDisplayed()
            captureNode(tag = "modal_sheet", name = "shopping-sheet-narrow")
            onNodeWithText("Save item").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Save item").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("shopping_content").performScrollToNode(hasText("Barista oat drink"))
            onNodeWithText("Barista oat drink").assertExists()
        }

    @Test
    fun nicknameIsSavedAndSurvivesLeavingAndReopeningSettings() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            ThemePreferences.set("LIGHT")
            setContent { Box(Modifier.fillMaxSize().testTag("loom_app")) { LoomDemoApp() } }
            waitForHome()
            onNodeWithTag("navigation_money").performClick()
            onNodeWithText("Everyday").assertExists()
            onNodeWithTag("navigation_home").performClick()
            onNodeWithTag("open_my_account").performClick()
            onNodeWithTag("edit_nickname").performScrollTo().performClick()
            onNodeWithTag("nickname_input").assertTextContains("You")
            onNodeWithTag("nickname_input").performTextReplacement("x".repeat(81))
            onNodeWithText("Save nickname").performClick()
            onNodeWithText("Use 1–80 characters for your nickname.").assertExists()
            onNodeWithTag("nickname_input").assertTextContains("x".repeat(81))
            onNodeWithTag("nickname_input").performTextReplacement("Rui")
            captureNode(tag = "modal_sheet", name = "nickname-sheet-light")
            onNodeWithText("Save nickname").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("Save nickname").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithContentDescription("Back").performClick()
            onNodeWithTag("navigation_money").performClick()
            onNodeWithTag("balances_view_balances").performClick()
            onNodeWithText("Rui").assertExists()
            onNodeWithTag("navigation_home").performClick()
            onNodeWithTag("open_my_account").performClick()
            onNodeWithTag("edit_nickname").performScrollTo().performClick()
            onNodeWithTag("nickname_input").assertTextContains("Rui")
            onNodeWithText("Cancel").performClick()
            onNodeWithTag("account_content").performScrollToNode(hasText("Dark"))
            onNodeWithText("Dark", useUnmergedTree = true).performClick()
            onNodeWithTag("edit_nickname").performScrollTo().performClick()
            captureNode(tag = "modal_sheet", name = "nickname-sheet-dark")
        }

    @Test
    fun emptyHomeShowsTodaysEmptyStateAndQuickActions() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            var openedExpense = false
            setContent {
                LoomTheme(mode = ThemeMode.LIGHT) {
                    Box(Modifier.fillMaxSize().testTag("empty_home")) {
                        HomeScreen(
                            viewModel = EmptyHomeViewModel(),
                            onAddExpense = { openedExpense = true },
                            onReminders = {},
                            onAddReminder = {},
                        )
                    }
                }
            }
            onNodeWithTag("home_reminders_empty").assertExists()
            onNodeWithTag("home_shopping_preview").assertDoesNotExist()
            captureNode(tag = "empty_home", name = "home-empty-light")
            onNodeWithTag("home_add_expense").performClick()
            assertTrue(openedExpense)
        }

    @Test
    fun anInitialLoadFailureNeverLooksLikeAnEmptyHouseholdAndRetryLoadsRealContent() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            val fixture = HomeRetryFixture()
            val viewModel = fixture.viewModel()
            try {
                setContent {
                    LoomTheme(mode = ThemeMode.LIGHT) {
                        HomeScreen(
                            viewModel = viewModel,
                            onAddExpense = {},
                            onReminders = {},
                            onAddReminder = {},
                        )
                    }
                }
                waitUntil(timeoutMillis = 5000) {
                    onAllNodesWithText("Try again").fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithText("Check your connection and try again.").assertExists()
                onNodeWithTag("home_reminders_empty").assertDoesNotExist()
                onNodeWithTag("home_shopping_preview").assertDoesNotExist()
                onNodeWithText("All settled up").assertDoesNotExist()
                onNodeWithText("Try again").performClick()
                waitUntil(timeoutMillis = 5000) { viewModel.state.value.hasOverview }
                onNodeWithText("Check your connection and try again.").assertDoesNotExist()
                assertEquals(2, fixture.loads)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun toolbarKeepsItsTitleAndBackButtonBelowTheStatusBar() =
        runSkikoComposeUiTest(size = Size(360f, 240f)) {
            setContent {
                LoomTheme(mode = ThemeMode.LIGHT) {
                    Toolbar(
                        text = "Create household",
                        showBackButton = true,
                        onBack = {},
                        windowInsets = WindowInsets(top = 30),
                        modifier = Modifier.testTag("toolbar"),
                    )
                }
            }
            val back = onNodeWithContentDescription("Back").fetchSemanticsNode().boundsInRoot
            val title = onNodeWithText("Create household").fetchSemanticsNode().boundsInRoot
            val toolbar = onNodeWithTag("toolbar").fetchSemanticsNode().boundsInRoot
            assertTrue(back.top >= 30f)
            assertTrue(title.top >= 30f)
            assertTrue(toolbar.height >= 86f)
        }

    private fun ComposeUiTest.waitForHome() {
        waitUntil(timeoutMillis = 10_000) {
            onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun SkikoComposeUiTest.captureNode(tag: String, name: String) {
        waitForIdle()
        onNodeWithTag(tag).assertIsDisplayed()
        val bitmap = captureToImage()
        val directory =
            File(System.getProperty("loom.screenshots.dir", "build/screenshots")).apply { mkdirs() }
        File(directory, "$name.png")
            .writeBytes(
                Image.makeFromBitmap(bitmap.asSkiaBitmap())
                    .encodeToData(EncodedImageFormat.PNG)!!
                    .bytes,
            )
    }
}
