package com.eyther.lumbridge.features.feed.domain.model

import com.eyther.lumbridge.core.domain.pagination.CursorPageDomain

/** A page of stories; the next cursor is absent at the end, including an exactly-full final page. */
typealias FeedPageDomain = CursorPageDomain<ArticleDomain, FeedCursorDomain>
