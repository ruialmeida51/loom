package com.eyther.lumbridge.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runSkikoComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.app.demo.LoomDemoApp
import com.eyther.lumbridge.app.home.data.EmptyHomeViewModel
import com.eyther.lumbridge.app.home.data.HomeRetryFixture
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.app.testutil.capture
import com.eyther.lumbridge.core.presentation.components.toolbar.Toolbar
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.home.presentation.screen.HomeScreen
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.cancel

@OptIn(ExperimentalTestApi::class)
class DesignFlowsUiTest {

    @Test
    fun shoppingRowsEditInPlaceAndTheKeyboardArrowAddsTheNextLineBelowOnANarrowViewport() =
        runSkikoComposeUiTest(
            size = Size(360f, 540f),
            density = Density(density = 1f, fontScale = 1.3f),
        ) {
            ThemePreferences.set("LIGHT")
            setContent { Box(Modifier.fillMaxSize().testTag("loom_app")) { LoomDemoApp() } }
            waitForHome()
            onNodeWithTag("navigation_shopping").performClick()
            awaitLoadedDestination(tag = "shopping_content")
            onNodeWithTag("edit_Avocados").performClick()
            onNodeWithTag("form_sheet").assertDoesNotExist()
            onNodeWithTag("edit_input_Avocados").assertIsFocused()
            onNodeWithTag("edit_input_Avocados").performTextReplacement("Hass avocados")
            onNodeWithTag("edit_input_Avocados").performImeAction()
            onNodeWithTag("shopping_draft_input").assertIsFocused()
            onNodeWithTag("shopping_draft_input").performTextInput("Limes")
            onNodeWithTag("shopping_draft_input").performImeAction()
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithTag("edit_Limes").fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("shopping_draft_input").assertIsFocused()
            capture(name = "shopping-keep-entry-narrow", tag = "loom_app")
            val rows =
                onAllNodes(hasTestTagPrefix("edit_")).fetchSemanticsNodes().map {
                    it.config[SemanticsProperties.TestTag]
                }
            assertEquals(
                rows.indexOf("edit_Hass avocados") + 1,
                rows.indexOf("edit_Limes"),
                "The new line sits directly below the edited row: $rows",
            )
            onNodeWithTag("actions_Limes").performClick()
            onNodeWithText("Move to another list").assertDoesNotExist()
            onNodeWithText("Group under previous item").assertDoesNotExist()
            onNodeWithText("Remove").assertIsDisplayed()
        }

    @Test
    fun nicknameIsSavedAndSurvivesLeavingAndReopeningSettings() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            ThemePreferences.set("LIGHT")
            setContent { Box(Modifier.fillMaxSize().testTag("loom_app")) { LoomDemoApp() } }
            waitForHome()
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithText("Everyday").assertExists()
            onNodeWithTag("navigation_home").performClick()
            onNodeWithTag("open_my_account").performClick()
            onNodeWithTag("edit_nickname").performScrollTo().performClick()
            onNodeWithTag("nickname_input").assertTextContains("You")
            onNodeWithTag("nickname_input").performTextReplacement("x".repeat(81))
            onNodeWithTag("save_nickname").performClick()
            onNodeWithText("Use 1–80 characters for your nickname.").assertExists()
            onNodeWithTag("nickname_input").assertTextContains("x".repeat(81))
            onNodeWithTag("nickname_input").performTextReplacement("Rui")
            capture(name = "nickname-sheet-light", tag = "modal_sheet")
            onNodeWithTag("save_nickname").performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("save_nickname").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithContentDescription("Back").performClick()
            onNodeWithTag("navigation_money").performClick()
            awaitLoadedDestination(tag = "expenses_content")
            onNodeWithTag("balances_view_balances").performClick()
            onNodeWithTag("member_balances_toggle").performScrollTo().performClick()
            onNodeWithTag("expenses_content").performScrollToNode(hasText("Rui"))
            onNodeWithText("Rui").assertExists()
            onNodeWithTag("navigation_home").performClick()
            onNodeWithTag("open_my_account").performClick()
            onNodeWithTag("edit_nickname").performScrollTo().performClick()
            onNodeWithTag("nickname_input").assertTextContains("Rui")
            onNodeWithText("Cancel").performClick()
            onNodeWithTag("appearance_preference").performScrollTo().performClick()
            onNodeWithText("Dark", useUnmergedTree = true).performClick()
            onNodeWithTag("edit_nickname").performScrollTo().performClick()
            capture(name = "nickname-sheet-dark", tag = "modal_sheet")
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
                            onTasks = {},
                            onShopping = {},
                            onAddTask = {},
                            onBalances = {},
                            onFeed = {},
                            onOpenStory = {},
                        )
                    }
                }
            }
            onNodeWithTag("home_tasks_empty").assertExists()
            onNodeWithTag("home_content").performScrollToNode(hasTestTag("home_shopping_preview"))
            onNodeWithTag("home_shopping_preview").assertExists()
            onNodeWithText("Nothing to pick up. You’re all stocked up.").assertExists()
            capture(name = "home-empty-light", tag = "empty_home")
            onNodeWithTag("home_content").performScrollToNode(hasTestTag("home_add_expense"))
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
                            onTasks = {},
                            onShopping = {},
                            onAddTask = {},
                            onBalances = {},
                            onFeed = {},
                            onOpenStory = {},
                        )
                    }
                }
                waitUntil(timeoutMillis = 5000) {
                    onAllNodesWithText("Try again").fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithText("Check your connection and try again.").assertExists()
                onNodeWithTag("home_tasks_empty").assertDoesNotExist()
                onNodeWithTag("home_content").performScrollToNode(hasTestTag("home_shopping_preview"))
                onNodeWithTag("home_shopping_preview").assertExists()
                onNodeWithText("All settled up").assertDoesNotExist()
                onNodeWithText("Try again").performClick()
                waitUntil(timeoutMillis = 5000) { viewModel.state.value.taskItems != null }
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
            onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
        }
    }


    private fun hasTestTagPrefix(prefix: String): SemanticsMatcher =
        SemanticsMatcher("TestTag starts with $prefix") { node ->
            node.config.getOrNull(SemanticsProperties.TestTag)?.let {
                it.startsWith(prefix) && !it.startsWith("edit_input_")
            } == true
        }
}
