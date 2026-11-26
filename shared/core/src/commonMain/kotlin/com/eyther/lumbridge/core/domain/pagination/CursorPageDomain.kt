package com.eyther.lumbridge.core.domain.pagination

/**
 * One page of a keyset-paged list and where the next one starts. [nextCursor] is null at the end,
 * including when the last page is exactly full, so "is there more" never depends on a total count
 * that may have changed while the reader scrolled.
 */
data class CursorPageDomain<out T, out C>(val items: List<T>, val nextCursor: C?) {
    val isLastPage: Boolean = nextCursor == null
}

/** How close to the end of a paged list (in rows) the next page starts loading. */
const val DEFAULT_PAGINATION_PREFETCH_THRESHOLD = 5
