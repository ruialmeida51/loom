package com.eyther.lumbridge.features.expenses.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.usecase.GetPaymentHandlesUseCase
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSettlementFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSettlementUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RecordSettlementUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RemoveSettlementUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveSettlementUseCase
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.PaymentDetailsScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.PaymentFormScreenViewModel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.PaymentDetailsScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.PaymentFormScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase

class PaymentWorkflowFixture {
    val references =
        ExpenseFormFixture().apply {
            accountResult.complete(AppResult.Success(account))
            membersResult.complete(AppResult.Success(members))
        }
    val payment =
        SettlementDomain(
            id = "payment",
            from = "you",
            to = "alex",
            amountMinor = 1250,
            occurredOn = "2026-11-18",
            accountId = references.account.id,
            createdBy = "you",
            version = 3,
        )
    val source =
        SettlementFixtureDataSource().apply { payment = this@PaymentWorkflowFixture.payment }
    internal var paymentHandles = PaymentHandlesFixtureDataSource()
    private val getSettlementFormUseCase =
        GetSettlementFormUseCase(
            expenseAccountDataSource = references.accounts,
            getSettlementUseCase = GetSettlementUseCase(expenseDataSource = source),
            householdDataSource = references.household,
            context = references.context,
            getPaymentHandlesUseCase = GetPaymentHandlesUseCase(paymentHandles),
        )

    fun form(
        editing: Boolean = true,
        suggestion: SuggestedSettlementDomain? = null,
    ): PaymentFormScreenViewModel =
        PaymentFormScreenViewModel(
            params =
                PaymentFormScreenViewModelContract.Params(
                    accountId = references.account.id,
                    paymentId = payment.id.takeIf { editing },
                    suggestion = suggestion,
                ),
            getSettlementFormUseCase = getSettlementFormUseCase,
            recordSettlementUseCase =
                RecordSettlementUseCase(
                    expenseDataSource = source,
                    expenseAccountDataSource = references.accounts,
                    members = GetMembersUseCase(householdDataSource = references.household),
                    context = references.context,
                ),
            saveSettlementUseCase = SaveSettlementUseCase(expenseDataSource = source),
            context = references.context,
        )

    fun details(): PaymentDetailsScreenViewModel =
        PaymentDetailsScreenViewModel(
            params =
                PaymentDetailsScreenViewModelContract.Params(
                    accountId = references.account.id,
                    paymentId = payment.id,
                ),
            getSettlementFormUseCase = getSettlementFormUseCase,
            removeSettlementUseCase = RemoveSettlementUseCase(expenseDataSource = source),
            context = references.context,
        )
}
