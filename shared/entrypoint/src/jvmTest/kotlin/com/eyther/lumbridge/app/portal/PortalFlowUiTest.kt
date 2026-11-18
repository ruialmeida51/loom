package com.eyther.lumbridge.app.portal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runSkikoComposeUiTest
import com.eyther.lumbridge.app.demo.data.DemoAnalyticsDataSource
import com.eyther.lumbridge.app.demo.data.DemoDiagnosticsDataSource
import com.eyther.lumbridge.app.household.data.HouseholdSetupFixtureDataSource
import com.eyther.lumbridge.app.navigation.PortalHost
import com.eyther.lumbridge.app.testutil.awaitLoadedDestination
import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.theme.LoomTheme
import com.eyther.lumbridge.core.presentation.theme.ThemeMode
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalTestApi::class)
class PortalFlowUiTest {
    private class ProfileFixture : ProfileDataSource {
        val profile =
            MutableStateFlow<AppResult<ProfileDomain>>(
                AppResult.Success(ProfileDomain(id = "member", nickname = "Yara")),
            )

        override fun stream(userId: String) = profile

        override suspend fun updateNickname(userId: String, nickname: String) =
            AppResult.Success(ProfileDomain(id = userId, nickname = nickname))
    }

    @Test
    fun myAccountWorksWithoutAHouseholdAndCreationOpensTheNewHousehold() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            val profiles = ProfileFixture()
            val households = HouseholdSetupFixtureDataSource()
            setContent {
                var selected by remember { mutableStateOf<HouseholdContextDomain?>(null) }
                LoomTheme(mode = ThemeMode.LIGHT) {
                    PortalHost(
                        userId = "member",
                        context = selected,
                        access = SupabaseAccess(client = null),
                        householdDataSource = households,
                        profileDataSource = profiles,
                        analyticsDataSource = DemoAnalyticsDataSource(),
                        diagnosticsDataSource = DemoDiagnosticsDataSource(),
                        onSelect = { household ->
                            selected =
                                HouseholdContextDomain(
                                    id = household.id,
                                    userId = "member",
                                    name = household.name,
                                    currency = household.currency,
                                    timeZone = household.timeZone,
                                )
                        },
                        onSignOut = {},
                    )
                }
            }
            onNodeWithText("My account").performClick()
            onNodeWithText("Profile").assertExists()
            onNodeWithContentDescription("Back").performClick()
            awaitLoadedDestination(tag = "households_content")
            onNodeWithTag("open_create_household").performClick()
            onNodeWithText("Household name").performTextInput("A new home")
            onNodeWithTag("create_household_submit").performScrollTo().performClick()
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithTag("home_content").fetchSemanticsNodes().isNotEmpty() &&
                    onAllNodesWithTag("loading_rows").fetchSemanticsNodes().isEmpty()
            }
            onNodeWithTag("household_switch").assertIsDisplayed()
            onNodeWithText("A new home").assertExists()
            onNodeWithTag("open_my_account").performClick()
            onNodeWithText("Yara").assertExists()
        }

    @Test
    fun globalProfileChangesUpdateTheHouseholdAvatarWithoutLeavingHome() =
        runSkikoComposeUiTest(size = Size(440f, 760f)) {
            val profiles = ProfileFixture()
            setContent {
                LoomTheme(mode = ThemeMode.LIGHT) {
                    PortalHost(
                        userId = "member",
                        context =
                            HouseholdContextDomain(
                                id = "home",
                                userId = "member",
                                name = "Our home",
                                currency = "EUR",
                                timeZone = "UTC",
                            ),
                        access = SupabaseAccess(client = null),
                        householdDataSource = HouseholdSetupFixtureDataSource(),
                        profileDataSource = profiles,
                        analyticsDataSource = DemoAnalyticsDataSource(),
                        diagnosticsDataSource = DemoDiagnosticsDataSource(),
                        onSelect = {},
                        onSignOut = {},
                    )
                }
            }
            waitUntil(timeoutMillis = 5_000) {
                onAllNodesWithText("Y", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("profile_avatar_initial", useUnmergedTree = true).assertTextEquals("Y")
            runOnIdle {
                profiles.profile.value =
                    AppResult.Success(ProfileDomain(id = "member", nickname = "Lena"))
            }
            waitUntil(timeoutMillis = 5000) {
                onAllNodesWithText("L", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
            }
            onNodeWithTag("home_content").assertExists()
        }
}
