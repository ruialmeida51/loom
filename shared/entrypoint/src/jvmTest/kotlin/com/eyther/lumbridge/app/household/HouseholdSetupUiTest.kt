package com.eyther.lumbridge.app.household

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.app.household.data.HouseholdSetupFixtureDataSource
import com.eyther.lumbridge.app.navigation.HouseholdSetupHost
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.di.applicationModules
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import org.koin.compose.KoinIsolatedContext
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@OptIn(ExperimentalTestApi::class)
class HouseholdSetupUiTest {

    @Test
    fun creationHasItsOwnDestinationAndOpensTheCreatedHousehold() {
        val graph = graph()
        var selected: HouseholdDomain? = null
        try {
            runComposeUiTest {
                setContent { TestContent(graph = graph, onSelect = { selected = it }) }
                onNodeWithText("Household name").assertDoesNotExist()
                onNodeWithText("Invitation code").assertDoesNotExist()
                onNodeWithTag("open_create_household").performClick()
                onNodeWithText("Invitation code").assertDoesNotExist()
                onNodeWithText("Household name").performTextInput("Little home")
                onNodeWithTag("create_household_submit").performScrollTo().performClick()
                waitUntil(timeoutMillis = 5000) { selected != null }
                assertEquals("Little home", selected?.name)
                assertEquals("EUR", selected?.currency)
            }
        } finally {
            graph.close()
        }
    }

    @Test
    fun joiningHasItsOwnDestinationAndRefreshesSelectionAfterSuccess() {
        val graph = graph()
        try {
            runComposeUiTest {
                setContent { TestContent(graph = graph, onSelect = {}) }
                onNodeWithTag("open_join_household").performClick()
                onNodeWithText("Household name").assertDoesNotExist()
                onNodeWithText("Invitation code").performTextInput("valid-household-invitation")
                onNodeWithTag("join_household_submit").performClick()
                waitUntil(timeoutMillis = 5000) {
                    onAllNodesWithText("Joined home").fetchSemanticsNodes().isNotEmpty()
                }
                onNodeWithText("Invitation code").assertDoesNotExist()
                onNodeWithContentDescription("Back").assertDoesNotExist()
            }
        } finally {
            graph.close()
        }
    }

    private fun graph(): KoinApplication = koinApplication {
        modules(applicationModules())
        modules(module { single<HouseholdDataSource> { HouseholdSetupFixtureDataSource() } })
    }

    @Composable
    private fun TestContent(graph: KoinApplication, onSelect: (HouseholdDomain) -> Unit) {
        KoinIsolatedContext(context = graph) {
            LoomTheme(mode = ThemeMode.LIGHT) {
                Box(Modifier.requiredSize(440.dp, 760.dp)) {
                    HouseholdSetupHost(userId = "test-member", onSelect = onSelect, onSignOut = {})
                }
            }
        }
    }
}
