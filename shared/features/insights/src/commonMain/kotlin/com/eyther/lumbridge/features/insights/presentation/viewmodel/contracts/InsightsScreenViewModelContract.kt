package com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.insights.domain.model.MonthlyStatementDomain
import kotlinx.coroutines.flow.*

data class InsightsState(
    val month: String = todayIso().take(7),
    val statement: MonthlyStatementDomain? = null,
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

abstract class InsightsScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<InsightsState>

    abstract fun previous()

    abstract fun next()

    abstract fun dismiss()

    abstract fun save()
}
