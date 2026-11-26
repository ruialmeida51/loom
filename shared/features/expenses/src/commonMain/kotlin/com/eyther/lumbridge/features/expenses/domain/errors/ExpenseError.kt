package com.eyther.lumbridge.features.expenses.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class ExpenseError(cause: Throwable? = null) : AppError(cause) {

    class InvalidPaymentDate(cause: Throwable? = null) : ExpenseError(cause)

    class FuturePaymentDate(cause: Throwable? = null) : ExpenseError(cause)

    class InvalidTitle(cause: Throwable? = null) : ExpenseError(cause)

    class InvalidMembers(cause: Throwable? = null) : ExpenseError(cause)

    class InvalidSettlement(cause: Throwable? = null) : ExpenseError(cause)

    class SettlementNotPermitted(cause: Throwable? = null) : ExpenseError(cause)

    class InvalidSettlementMembers(cause: Throwable? = null) : ExpenseError(cause)

    class UnsupportedCurrency(cause: Throwable? = null) : ExpenseError(cause)

    class InvalidAmountPrecision(cause: Throwable? = null) : ExpenseError(cause)

    class AmountTooLarge(cause: Throwable? = null) : ExpenseError(cause)

    class AmountNotPositive(cause: Throwable? = null) : ExpenseError(cause)

    class UnbalancedLedger(cause: Throwable? = null) : ExpenseError(cause)

    class InvalidAmount(cause: Throwable? = null) : ExpenseError(cause)

    class DuplicateParticipants(cause: Throwable? = null) : ExpenseError(cause)

    class InvalidPercentages(cause: Throwable? = null) : ExpenseError(cause)

    class NoParticipants(cause: Throwable? = null) : ExpenseError(cause)

    class InvalidShare(cause: Throwable? = null) : ExpenseError(cause)

    class IncorrectSplitTotal(cause: Throwable? = null) : ExpenseError(cause)

    class AccessDenied(cause: Throwable? = null) : ExpenseError(cause)

    class AlreadyRecorded(cause: Throwable? = null) : ExpenseError(cause)

    /** The amount in another currency, its rate or its conversion is not usable. */
    class InvalidConversion(cause: Throwable? = null) : ExpenseError(cause)

    /** Recurring bills repeat in the account's own currency. */
    class RecurringSingleCurrency(cause: Throwable? = null) : ExpenseError(cause)

    /** Today's rate could not be fetched; the person can type one. */
    class RateUnavailable(cause: Throwable? = null) : ExpenseError(cause)
}
