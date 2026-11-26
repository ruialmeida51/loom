package com.eyther.lumbridge.features.shopping.domain.model

/**
 * A shopping item's name split into plain text and links, so a pasted product page shows as its
 * site ("ikea.com") and opens in the browser instead of filling the row with a long address.
 */
sealed interface ShoppingNamePartDomain {
    data class Text(val value: String) : ShoppingNamePartDomain

    /** [url] is what opens (always http or https); [label] is the site it names. */
    data class Link(val url: String, val label: String) : ShoppingNamePartDomain

    companion object {
        // http(s) addresses, or bare www. ones as people often paste them.
        private val Address = Regex("""(?i)\b(?:https?://|www\.)[^\s<>"]+""")
        private const val TRAILING_PUNCTUATION = ".,;:!?'\"]}"

        fun parse(text: String): List<ShoppingNamePartDomain> {
            val parts = mutableListOf<ShoppingNamePartDomain>()
            var start = 0
            for (match in Address.findAll(text)) {
                val raw = match.value.trimLink()
                val link = raw.toLink() ?: continue
                if (match.range.first > start) parts += Text(text.substring(start, match.range.first))
                parts += link
                start = match.range.first + raw.length
            }
            if (start < text.length) parts += Text(text.substring(start))
            return parts.ifEmpty { listOf(Text(text)) }
        }

        /** Sentence punctuation after a pasted address is not part of it; nor is an unmatched ")". */
        private fun String.trimLink(): String {
            var end = length
            while (end > 0) {
                val last = this[end - 1]
                val unmatchedParenthesis =
                    last == ')' && substring(0, end).count { it == '(' } < substring(0, end).count { it == ')' }
                if (last in TRAILING_PUNCTUATION || unmatchedParenthesis) end-- else break
            }
            return substring(0, end)
        }

        private fun String.toLink(): Link? {
            val url = if (startsWith("www.", ignoreCase = true)) "https://$this" else this
            val host =
                url.substringAfter("://")
                    .substringBefore('/')
                    .substringBefore('?')
                    .substringBefore('#')
                    .substringAfterLast('@')
                    .substringBefore(':')
                    .lowercase()
            if ('.' !in host || host.startsWith('.') || host.endsWith('.')) return null
            return Link(url = url, label = host.removePrefix("www."))
        }
    }
}
