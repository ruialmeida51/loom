package com.eyther.lumbridge.features.household.domain

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.data.fixtures.FakeHouseholdDataSource
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError
import com.eyther.lumbridge.features.household.domain.usecase.UpdateMemberNicknameUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

class UpdateMemberNicknameTest {
    @Test
    fun trimsTheSavedNicknameWithoutDiscardingInternationalNames() = runTest {
        val source = FakeHouseholdDataSource()
        val update = UpdateMemberNicknameUseCase(householdDataSource = source)
        assertIs<AppResult.Success<Unit>>(update(userId = "member", nickname = "  雨 / José  "))
        assertEquals("member" to "雨 / José", source.savedNickname)
    }

    @Test
    fun invalidNamesCannotReachTheDataSource() = runTest {
        val source = FakeHouseholdDataSource()
        val update = UpdateMemberNicknameUseCase(householdDataSource = source)
        for (name in listOf("", "   ", "a\nb", "a\tb", "x".repeat(81))) {
            assertIs<HouseholdError.InvalidNickname>(
                assertIs<AppResult.Failure>(update(userId = "member", nickname = name)).error,
            )
        }
        assertNull(source.savedNickname)
    }

    @Test
    fun aFailedWriteKeepsItsTypedFailure() = runTest {
        val source = FakeHouseholdDataSource()
        val failure = AppResult.Failure(CommonError.NoConnection())
        source.saveResult = failure
        assertSame(
            failure,
            UpdateMemberNicknameUseCase(householdDataSource = source)(
                userId = "member",
                nickname = "New name",
            ),
        )
    }
}
