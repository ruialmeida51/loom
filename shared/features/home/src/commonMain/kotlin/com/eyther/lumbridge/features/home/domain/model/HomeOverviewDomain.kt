package com.eyther.lumbridge.features.home.domain.model

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import kotlinx.coroutines.flow.*

data class HomeOverviewDomain(
    val shopping: List<ShoppingItemDomain>,
    val reminders: List<ReminderDomain>,
    val balanceMinor: Long,
    val monthSpendingMinor: Long,
)
