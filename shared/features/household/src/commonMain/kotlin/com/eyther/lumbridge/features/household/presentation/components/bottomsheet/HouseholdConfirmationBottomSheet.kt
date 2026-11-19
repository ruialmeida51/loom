package com.eyther.lumbridge.features.household.presentation.components.bottomsheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.management_confirm_name
import com.eyther.lumbridge.features.household.presentation.model.HouseholdConfirmationContentUi
import org.jetbrains.compose.resources.stringResource

/** Confirmation content is household-owned; the Host composes financial impact from Expenses. */
@Composable
fun HouseholdConfirmationBottomSheet(
    confirmation: HouseholdConfirmationContentUi,
    name: String,
    confirmationName: String,
    busy: Boolean,
    canConfirm: Boolean,
    error: TextResource?,
    onNameChanged: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    balanceContent: @Composable () -> Unit,
) {
    FormBottomSheet(
        title = confirmation.title.getString(),
        actionLabel = confirmation.action.getString(),
        onAction = onConfirm,
        onDismiss = onDismiss,
        busy = busy,
        destructiveAction = confirmation.destructive,
        actionEnabled = canConfirm,
        actionModifier = Modifier.testTag("confirm_household_action"),
    ) {
        Text(confirmation.message.getString())
        balanceContent()
        if (confirmation.requiresName)
            OutlinedTextField(
                value = confirmationName,
                onValueChange = onNameChanged,
                label = { Text(stringResource(Res.string.management_confirm_name, name)) },
                singleLine = true,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().testTag("household_confirmation_name"),
            )
        error?.let { ErrorNotice(message = it) }
    }
}
