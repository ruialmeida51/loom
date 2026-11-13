package com.eyther.lumbridge.features.insights.presentation.localization

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.core.presentation.localization.genericErrorMessage
import com.eyther.lumbridge.features.insights.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun localizedMessage(code: String?): String? =
    when {
        code == null -> null
        code.contains(
            "insights.message_month_currency_household_total_minor_units_you_paid_minor",
        ) ->
            stringResource(
                Res.string.message_month_currency_household_total_minor_units_you_paid_minor,
            )
        code.contains(
            "insights.message_date_description_category_amount_minor_units_your_share_minor",
        ) ->
            stringResource(
                Res.string.message_date_description_category_amount_minor_units_your_share_minor,
            )
        code.contains("insights.message_statement_saved_later_changes_create_a_new_revision") ->
            stringResource(Res.string.message_statement_saved_later_changes_create_a_new_revision)
        else -> genericErrorMessage()
    }
