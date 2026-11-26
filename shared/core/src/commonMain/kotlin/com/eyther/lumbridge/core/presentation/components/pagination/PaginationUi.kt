package com.eyther.lumbridge.core.presentation.components.pagination

import androidx.compose.runtime.Immutable
import com.eyther.lumbridge.core.presentation.resources.TextResource

/**
 * The paging part of a list's state, kept in its ViewModel. A failed append sets [loadMoreError]
 * and stops paging until the reader retries; otherwise the list end, now visible again without its
 * loading row, would ask for the failing page in a loop.
 */
@Immutable
data class PaginationUi(
    val hasMore: Boolean = false,
    val loadingMore: Boolean = false,
    val loadMoreError: TextResource? = null,
    /** False while the list as a whole is loading, failed or showing another view. */
    val enabled: Boolean = true,
) {
    val canLoadMore: Boolean = enabled && hasMore && !loadingMore && loadMoreError == null
}
