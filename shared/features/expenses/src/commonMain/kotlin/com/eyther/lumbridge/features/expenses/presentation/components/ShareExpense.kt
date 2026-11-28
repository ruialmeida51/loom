package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.core.presentation.share.rememberTextSharer
import com.eyther.lumbridge.features.expenses.presentation.mapper.expenseShareText
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseDetailsUi
import kotlinx.coroutines.launch

/** Sharing [details] as text from the details toolbar; null until the expense has loaded. */
@Composable
fun rememberShareExpense(details: ExpenseDetailsUi?): (() -> Unit)? {
    val share = rememberTextSharer()
    val scope = rememberCoroutineScope()
    val date = details?.let { calendarDateText(it.occurredOn) } ?: return null
    return { scope.launch { share(expenseShareText(details = details, date = date)) } }
}
