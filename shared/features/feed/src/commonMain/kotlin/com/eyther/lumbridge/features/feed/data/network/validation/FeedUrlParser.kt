package com.eyther.lumbridge.features.feed.data.network.validation

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.features.feed.domain.errors.FeedError
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException

private const val MAX_FEED_URL_LENGTH = 2048
private val hostLabel = Regex("[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?")

/**
 * Parses HTTPS addresses before writes, rejecting authorities Ktor would silently default.
 * Publisher access remains server policy.
 */
fun parseFeedUrl(value: String): String {
    val input = value.trim()
    requireDomain(input.length <= MAX_FEED_URL_LENGTH && input.none { it.isWhitespace() || it.isISOControl() }) {
        FeedError.InvalidUrl()
    }

    val authority = input.substringAfter("://", missingDelimiterValue = "")
        .substringBefore('/').substringBefore('?').substringBefore('#')
    requireDomain(authority.isNotEmpty()) { FeedError.InvalidUrl() }
    val explicitPort = authority.substringAfterLast(':', missingDelimiterValue = "")
    requireDomain(':' !in authority || explicitPort.toIntOrNull()?.let { it in 1..65535 } == true) {
        FeedError.InvalidUrl()
    }

    val parsed = try {
        Url(input)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        throw FeedError.InvalidUrl(cause = error)
    }
    requireDomain(
        parsed.protocol == URLProtocol.HTTPS &&
            parsed.host.isNotEmpty() &&
            parsed.host.split('.').all { hostLabel.matches(it) } &&
            parsed.port in 1..65535 &&
            parsed.user.isNullOrEmpty() && parsed.password.isNullOrEmpty(),
    ) { FeedError.InvalidUrl() }

    return URLBuilder(parsed).apply {
        host = host.lowercase()
        fragment = ""
    }.buildString()
}
