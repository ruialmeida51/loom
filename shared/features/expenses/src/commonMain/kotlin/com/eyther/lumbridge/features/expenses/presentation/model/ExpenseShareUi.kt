package com.eyther.lumbridge.features.expenses.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class ExpenseShareUi(
    val id: String,
    val name: String?,
    val amount: String,
    val isCurrentUser: Boolean,
    val isPayer: Boolean,
    val avatarPath: String? = null,
    val isGuest: Boolean = false,
)
