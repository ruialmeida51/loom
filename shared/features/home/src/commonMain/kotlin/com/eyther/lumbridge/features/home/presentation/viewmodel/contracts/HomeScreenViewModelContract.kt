package com.eyther.lumbridge.features.home.presentation.viewmodel.contracts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain
import kotlinx.coroutines.flow.*

data class HomeState(val overview: HomeOverviewDomain? = null, val error: String? = null)

abstract class HomeScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<HomeState>

    abstract fun dismiss()
}
