package com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlinx.coroutines.flow.StateFlow

abstract class RemindersScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<RemindersState>

    abstract fun onRefresh()

    abstract fun onAddSheetVisibilityChanged(value: Boolean)

    abstract fun onTitleChanged(value: String)

    abstract fun onDateChanged(value: String)

    abstract fun onTimeChanged(value: String)

    abstract fun onRecurrenceChanged(value: String)

    abstract fun onDismissError()

    abstract fun onSaveReminder()

    abstract fun onCompleteReminder(item: ReminderDomain)

    abstract fun onRemoveReminder(id: String)
}
