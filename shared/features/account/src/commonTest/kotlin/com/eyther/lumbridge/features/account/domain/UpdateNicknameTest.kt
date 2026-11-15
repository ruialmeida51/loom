package com.eyther.lumbridge.features.account.domain

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.data.fixtures.FakeProfileDataSource
import com.eyther.lumbridge.features.account.domain.errors.AccountError
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain
import com.eyther.lumbridge.features.account.domain.usecase.UpdateNicknameUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

class UpdateNicknameTest {
    @Test
    fun trimsTheSavedNicknameWithoutDiscardingInternationalNames() = runTest {
        val source = FakeProfileDataSource()
        val update = UpdateNicknameUseCase(profileDataSource = source)
        assertIs<AppResult.Success<ProfileDomain>>(
            update(userId = "member", nickname = "  雨 / José  "),
        )
        assertEquals("member" to "雨 / José", source.savedNickname)
    }

    @Test
    fun invalidNamesCannotReachTheDataSource() = runTest {
        val source = FakeProfileDataSource()
        val update = UpdateNicknameUseCase(profileDataSource = source)
        for (name in listOf("", "   ", "a\nb", "a\tb", "x".repeat(81))) {
            assertIs<AccountError.InvalidNickname>(
                assertIs<AppResult.Failure>(update(userId = "member", nickname = name)).error,
            )
        }
        assertNull(source.savedNickname)
    }

    @Test
    fun aFailedWriteKeepsItsTypedFailure() = runTest {
        val source = FakeProfileDataSource()
        val failure = AppResult.Failure(CommonError.NoConnection())
        source.failure = failure
        assertSame(
            failure,
            UpdateNicknameUseCase(profileDataSource = source)(
                userId = "member",
                nickname = "New name",
            ),
        )
    }
}
