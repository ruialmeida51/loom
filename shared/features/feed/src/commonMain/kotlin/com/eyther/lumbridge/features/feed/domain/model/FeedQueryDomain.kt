package com.eyther.lumbridge.features.feed.domain.model

/** Empty source selection means every source; unread applies before page boundaries. */
data class FeedQueryDomain(
    val subscriptionIds: Set<String> = emptySet(),
    val unreadOnly: Boolean = false,
    val pageSize: Int = DEFAULT_PAGE_SIZE,
) {
    init {
        require(pageSize in 1..MAX_PAGE_SIZE)
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        private const val MAX_PAGE_SIZE = 100
    }
}
