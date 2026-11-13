package com.eyther.lumbridge.features.home.presentation.viewmodel

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.home.domain.usecase.GetHomeOverviewUseCase
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.HomeState
import kotlinx.coroutines.flow.*

class HomeScreenViewModel(private val getHomeOverviewUseCase: GetHomeOverviewUseCase) :
    HomeScreenViewModelContract() {
    private val mutableState = MutableStateFlow(HomeState())
    override val state = mutableState.asStateFlow()

    init {
        mutableState.observeWith(viewModelScope) {
            getHomeOverviewUseCase()
                .catch { error -> mutableState.update { it.copy(error = error.message) } }
                .onEach { overview -> mutableState.update { it.copy(overview = overview) } }
        }
    }

    override fun dismiss() {
        mutableState.update { it.copy(error = null) }
    }
}
