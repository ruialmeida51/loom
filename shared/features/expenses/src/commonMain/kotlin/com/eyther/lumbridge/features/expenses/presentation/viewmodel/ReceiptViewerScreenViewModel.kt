package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.usecase.DownloadReceiptPhotoUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseReceiptUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.model.ReceiptPhotoUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ReceiptViewerScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ReceiptViewerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ReceiptViewerScreenViewModel(
    private val params: ReceiptViewerScreenViewModelContract.Params,
    private val getExpenseReceiptUseCase: GetExpenseReceiptUseCase,
    private val downloadReceiptPhotoUseCase: DownloadReceiptPhotoUseCase,
) : ReceiptViewerScreenViewModelContract() {
    private val mutableState =
        MutableStateFlow(ReceiptViewerState()).launchWith(viewModelScope) { state ->
            loadPhoto(state)
        }
    override val state = mutableState.asStateFlow()

    override fun onRetry() {
        if (state.value.loading) return
        mutableState.update { oldState -> oldState.copy(loading = true, error = null) }
        launch { loadPhoto(mutableState) }
    }

    override fun onInvalidPhoto() {
        mutableState.update { oldState ->
            oldState.copy(
                photo = null,
                error = ReceiptError.InvalidPhoto().toExpensesTextResource(),
            )
        }
    }

    private suspend fun loadPhoto(state: MutableStateFlow<ReceiptViewerState>) {
        val result = delayAtLeast {
            safeCall {
                val receipt =
                    getExpenseReceiptUseCase(
                            expenseId = params.expenseId,
                            accountId = params.accountId,
                        )
                        .getOrThrow()
                downloadReceiptPhotoUseCase(receipt = receipt).getOrThrow()
            }
        }
        state.update { oldState ->
            when (result) {
                is AppResult.Success ->
                    oldState.copy(
                        loading = false,
                        photo = ReceiptPhotoUi(id = result.value.id, data = result.value.data),
                    )
                is AppResult.Failure ->
                    oldState.copy(loading = false, error = result.error.toExpensesTextResource())
            }
        }
    }
}
