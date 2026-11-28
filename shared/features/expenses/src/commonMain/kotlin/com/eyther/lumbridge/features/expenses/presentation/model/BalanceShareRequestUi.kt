package com.eyther.lumbridge.features.expenses.presentation.model

import com.eyther.lumbridge.features.expenses.domain.model.AccountSpendingDomain

/**
 * A share of the selected account waiting for the screen to send it; [spending] is the trip
 * summary, or null for Everyday or when it could not be loaded.
 */
data class BalanceShareRequestUi(val spending: AccountSpendingDomain? = null)
