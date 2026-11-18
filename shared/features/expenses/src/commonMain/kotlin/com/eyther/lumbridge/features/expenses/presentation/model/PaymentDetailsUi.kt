package com.eyther.lumbridge.features.expenses.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
data class PaymentDetailsUi(
    val amount: String,
    val fromName: String?,
    val toName: String?,
    val isUserSender: Boolean,
    val isUserRecipient: Boolean,
    val occurredOn: LocalDate,
    val accountName: String,
    val isEveryday: Boolean,
)
