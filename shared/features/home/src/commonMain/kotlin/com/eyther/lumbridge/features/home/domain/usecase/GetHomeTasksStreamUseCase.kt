package com.eyther.lumbridge.features.home.domain.usecase

import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.mapToResult
import com.eyther.lumbridge.features.home.domain.model.HomeTasksDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.usecase.GetTasksStreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetHomeTasksStreamUseCase(
    private val getTasksStreamUseCase: GetTasksStreamUseCase,
    private val context: HouseholdContextDomain,
) {
    operator fun invoke(): Flow<AppResult<HomeTasksDomain>> =
        getTasksStreamUseCase().map { result ->
            result.mapToResult { tasks ->
                HomeTasksDomain(
                    today = todayIso(context.timeZone),
                    tasks =
                        tasks
                            .filterNot { it.completed }
                            .sortedWith(
                                compareBy<TaskDomain> { it.dueAt == null }
                                    .thenBy { it.dueAt }
                                    .thenBy { it.id },
                            ),
                )
            }
        }
}
