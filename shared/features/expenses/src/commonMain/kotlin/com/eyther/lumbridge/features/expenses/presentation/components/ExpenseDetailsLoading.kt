package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.components.LoadingLine
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomCard

@Composable
internal fun ExpenseDetailsLoading() {
    LoomCard {
        LoadingLine(widthFraction = .4f, style = MaterialTheme.typography.labelMedium)
        LoadingLine(widthFraction = .7f, style = MaterialTheme.typography.titleLarge)
        LoadingLine(widthFraction = .5f, style = MaterialTheme.typography.displaySmall)
    }
    LoomCard { LoadingRows(rows = 3) }
    LoomCard { LoadingRows(rows = 3) }
}
