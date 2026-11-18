package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_retry
import com.eyther.lumbridge.features.expenses.presentation.components.receipt.ReceiptPhotoViewer
import com.eyther.lumbridge.features.expenses.presentation.model.ReceiptPhotoUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ReceiptViewerScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReceiptViewerScreen(viewModel: ReceiptViewerScreenViewModelContract) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerReceiptViewerScreen(
        photo = state.photo,
        loading = state.loading,
        error = state.error,
        onRetry = viewModel::onRetry,
        onInvalidPhoto = viewModel::onInvalidPhoto,
    )
}

@Composable
private fun InnerReceiptViewerScreen(
    photo: ReceiptPhotoUi?,
    loading: Boolean,
    error: TextResource?,
    onRetry: () -> Unit,
    onInvalidPhoto: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(LoomSpacing.content).testTag("receipt_viewer"),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
    ) {
        if (loading) LoadingRows()
        error?.let {
            ErrorNotice(message = it)
            LoomSecondaryButton(text = stringResource(Res.string.balance_retry), onClick = onRetry)
        }
        photo?.let {
            ReceiptPhotoViewer(photo = it, onInvalidPhoto = onInvalidPhoto)
        }
    }
}
