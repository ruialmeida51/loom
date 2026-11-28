package com.eyther.lumbridge.features.account.domain.model

/**
 * How someone likes to be paid back, each optional: an MB Way phone number in international
 * form (+351…), a Revolut username and a PayPal.me username. Shared only with housemates.
 */
data class PaymentHandlesDomain(
    val userId: String,
    val mbwayPhone: String? = null,
    val revolutUsername: String? = null,
    val paypalUsername: String? = null,
) {
    val isEmpty: Boolean = mbwayPhone == null && revolutUsername == null && paypalUsername == null

    /**
     * Cleans what people type or paste: spaces and dashes in phone numbers, a Portuguese number
     * without its +351, a leading @, or a whole revolut.me or paypal.me link.
     */
    fun normalized(): PaymentHandlesDomain =
        copy(
            mbwayPhone =
                mbwayPhone
                    ?.filter { it.isDigit() || it == '+' }
                    ?.let { digits ->
                        when {
                            digits.isEmpty() -> null
                            digits.startsWith("00") -> "+" + digits.drop(2)
                            digits.startsWith("+") -> digits
                            digits.length == PORTUGUESE_DIGITS && digits.first() == '9' ->
                                "+351$digits"
                            else -> digits
                        }
                    },
            revolutUsername =
                revolutUsername
                    ?.trim()
                    ?.substringAfter("revolut.me/")
                    ?.removePrefix("@")
                    ?.trimEnd('/')
                    ?.lowercase()
                    ?.ifEmpty { null },
            paypalUsername =
                paypalUsername
                    ?.trim()
                    ?.substringAfter("paypal.me/")
                    ?.removePrefix("@")
                    ?.substringBefore('/')
                    ?.ifEmpty { null },
        )

    /** Whether each set value has the shape the server accepts. */
    fun isValid(): Boolean =
        (mbwayPhone == null || PHONE.matches(mbwayPhone)) &&
            (revolutUsername == null || REVOLUT.matches(revolutUsername)) &&
            (paypalUsername == null || PAYPAL.matches(paypalUsername))

    /** A PayPal.me link that opens with [amount] (major units, e.g. "12.50") filled in. */
    fun paypalLink(amount: String, currency: String): String? =
        paypalUsername?.let { "https://paypal.me/$it/$amount$currency" }

    /** Their PayPal.me page, where the person paying types the amount. */
    fun paypalProfileLink(): String? = paypalUsername?.let { "https://paypal.me/$it" }

    fun revolutLink(): String? = revolutUsername?.let { "https://revolut.me/$it" }

    private companion object {
        const val PORTUGUESE_DIGITS = 9
        val PHONE = Regex("^\\+[1-9][0-9]{7,14}$")
        val REVOLUT = Regex("^[a-z0-9][a-z0-9._-]{2,31}$")
        val PAYPAL = Regex("^[A-Za-z0-9]{1,20}$")
    }
}
