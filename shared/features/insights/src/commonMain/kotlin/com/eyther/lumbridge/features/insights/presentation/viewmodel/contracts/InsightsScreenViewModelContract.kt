package com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class InsightsScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<InsightsState>

    abstract fun onPreviousMonth()

    abstract fun onNextMonth()

    abstract fun onDismissMessage()

    abstract fun onSaveStatement()

    abstract fun onExportStatement(summaryHeader: String, expenseHeader: String, title: String)
}
