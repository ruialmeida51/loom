package com.eyther.lumbridge.features.home.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeOverviewStreamUseCase
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeScreenViewModel(
    private val getHomeOverviewStreamUseCase: GetHomeOverviewStreamUseCase,
) : HomeScreenViewModelContract() {

    private val mutableState = MutableStateFlow(HomeState())
        .observeWith(viewModelScope) { state ->
            getHomeOverviewStreamUseCase().updateState(state) { result ->
                when (result) {
                    is AppResult.Success -> copy(
                        overview = result.value,
                        error = null,
                    )

                    is AppResult.Failure -> copy(
                        error = result.error.toTextResource(),
                    )
                }
            }
        }

    override val state = mutableState.asStateFlow()

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null)
        }
    }
}
