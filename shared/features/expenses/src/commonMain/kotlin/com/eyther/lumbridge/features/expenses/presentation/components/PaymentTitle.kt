package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.generated.resources.payment_i_paid_person
import com.eyther.lumbridge.features.expenses.generated.resources.payment_person_paid_me
import com.eyther.lumbridge.features.expenses.generated.resources.payment_personal_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun paymentTitle(
    fromName: String?,
    toName: String?,
    isUserSender: Boolean,
    isUserRecipient: Boolean,
): String =
    when {
        isUserSender ->
            stringResource(
                Res.string.payment_i_paid_person,
                toName ?: stringResource(Res.string.former_member),
            )
        isUserRecipient ->
            stringResource(
                Res.string.payment_person_paid_me,
                fromName ?: stringResource(Res.string.former_member),
            )
        else ->
            stringResource(
                Res.string.payment_personal_title,
                fromName ?: stringResource(Res.string.former_member),
                toName ?: stringResource(Res.string.former_member),
            )
    }
