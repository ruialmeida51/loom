package com.eyther.lumbridge.features.home.domain.model

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain

data class HomeOverviewDomain(
    val today: String = "",
    val reminders: List<ReminderDomain>,
    val shoppingItems: List<ShoppingItemDomain> = emptyList(),
    val shoppingError: AppError? = null,
)
