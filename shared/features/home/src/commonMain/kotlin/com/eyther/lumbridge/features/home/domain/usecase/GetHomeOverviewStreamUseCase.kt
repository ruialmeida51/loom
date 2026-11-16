package com.eyther.lumbridge.features.home.domain.usecase

import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingSelectionStreamUseCase
import com.eyther.lumbridge.features.shopping.domain.usecase.GetShoppingStreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetHomeOverviewStreamUseCase(
    private val getRemindersStreamUseCase: GetRemindersStreamUseCase,
    private val getShoppingSelectionStreamUseCase: GetShoppingSelectionStreamUseCase,
    private val getShoppingStreamUseCase: GetShoppingStreamUseCase,
    private val context: HouseholdContextDomain,
) {
    operator fun invoke(): Flow<AppResult<HomeOverviewDomain>> =
        combine(
            getRemindersStreamUseCase(),
            getShoppingStreamUseCase(),
            getShoppingSelectionStreamUseCase(),
        ) { reminders, shopping, selectedListId ->
            safeCall {
                val pending =
                    reminders.getOrThrow().filterNot { it.completed }.sortedBy { it.dueAt }
                val snapshot = (shopping as? AppResult.Success)?.value
                val listId = snapshot?.resolveActiveListId(selectedListId.orEmpty())
                HomeOverviewDomain(
                    today = todayIso(context.timeZone),
                    reminders = pending,
                    shoppingItems =
                        snapshot?.activeItems.orEmpty().filter {
                            !it.checked && it.listId == listId
                        },
                    shoppingError = (shopping as? AppResult.Failure)?.error,
                )
            }
        }
}
