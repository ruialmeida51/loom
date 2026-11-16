package com.eyther.lumbridge.features.feed.domain.model

/**
 * Publisher metadata for an optional starter source; it never subscribes a household implicitly.
 */
data class StarterFeedDomain(val publisher: String, val url: String)
