package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.DialogActions
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.OriginalAmountDomain
import com.eyther.lumbridge.features.expenses.domain.model.TravelCurrencyDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.currency_change_rate
import com.eyther.lumbridge.features.expenses.generated.resources.currency_choose_description
import com.eyther.lumbridge.features.expenses.generated.resources.currency_converted
import com.eyther.lumbridge.features.expenses.generated.resources.currency_rate
import com.eyther.lumbridge.features.expenses.generated.resources.currency_rate_label
import com.eyther.lumbridge.features.expenses.generated.resources.currency_rate_loading
import com.eyther.lumbridge.features.expenses.generated.resources.currency_rate_title
import com.eyther.lumbridge.features.expenses.generated.resources.currency_rate_today
import com.eyther.lumbridge.features.expenses.generated.resources.currency_rate_yours
import com.eyther.lumbridge.features.expenses.generated.resources.form_amount_label
import com.eyther.lumbridge.features.expenses.generated.resources.form_description
import com.eyther.lumbridge.features.expenses.generated.resources.form_save
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import org.jetbrains.compose.resources.stringResource

/** Draft capture is independent of reference-data loading; IME Next moves into the description. */
@Composable
internal fun ExpenseCaptureFields(
    amount: String,
    title: String,
    currency: String?,
    enabled: Boolean,
    onAmount: (String) -> Unit,
    onTitle: (String) -> Unit,
    onCurrency: ((String) -> Unit)? = null,
    conversion: ConversionUi? = null,
    onChangeRate: (String) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var currencyMenu by remember { mutableStateOf(false) }
    val currencyDescription =
        stringResource(Res.string.currency_choose_description, currency.orEmpty())
    OutlinedTextField(
        value = amount,
        onValueChange = onAmount,
        label = { Text(stringResource(Res.string.form_amount_label)) },
        suffix =
            if (onCurrency == null) ({ currency?.let { Text(it) } })
            else null,
        trailingIcon =
            if (onCurrency == null || currency == null) null
            else {
                {
                    Box {
                        Surface(
                            onClick = { currencyMenu = true },
                            enabled = enabled,
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            modifier =
                                Modifier.padding(end = 8.dp)
                                    .heightIn(min = 40.dp)
                                    .testTag("expense_currency")
                                    .semantics { contentDescription = currencyDescription },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(currency, style = MaterialTheme.typography.labelLarge)
                                LoomIcon(
                                    glyph = Glyph.CHEVRON_DOWN,
                                    decorative = true,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = currencyMenu,
                            onDismissRequest = { currencyMenu = false },
                            modifier = Modifier.heightIn(max = 360.dp),
                        ) {
                            TravelCurrencyDomain.codes.forEach { code ->
                                DropdownMenuItem(
                                    text = { Text(code) },
                                    trailingIcon = {
                                        if (code == currency)
                                            LoomIcon(glyph = Glyph.CHECK, decorative = true)
                                    },
                                    onClick = {
                                        currencyMenu = false
                                        onCurrency(code)
                                    },
                                    modifier = Modifier.testTag("currency_$code"),
                                )
                            }
                        }
                    }
                }
            },
        textStyle = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
        keyboardOptions =
            KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().testTag("expense_amount"),
    )
    conversion?.let { ConversionCard(conversion = it, enabled = enabled, onChangeRate = onChangeRate) }
    OutlinedTextField(
        value = title,
        onValueChange = onTitle,
        label = { Text(stringResource(Res.string.form_description)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().testTag("expense_title"),
    )
}

/** What a foreign amount becomes in the account, at which rate, and a way to change the rate. */
@Immutable
internal data class ConversionUi(
    val from: String,
    val to: String,
    val converted: String?,
    val rate: String?,
    val custom: Boolean,
    val loading: Boolean,
    val error: TextResource?,
)

@Composable
private fun ConversionCard(conversion: ConversionUi, enabled: Boolean, onChangeRate: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.fillMaxWidth().testTag("expense_conversion"),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                when {
                    conversion.loading ->
                        Text(
                            stringResource(Res.string.currency_rate_loading),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    conversion.error != null && conversion.rate == null ->
                        Text(conversion.error.getString(), style = MaterialTheme.typography.bodyMedium)
                    else -> {
                        conversion.converted?.let {
                            Text(
                                stringResource(Res.string.currency_converted, it),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                        conversion.rate?.let { rate ->
                            Text(
                                stringResource(
                                    Res.string.currency_rate,
                                    conversion.from,
                                    rate,
                                    conversion.to,
                                    stringResource(
                                        if (conversion.custom) Res.string.currency_rate_yours
                                        else Res.string.currency_rate_today,
                                    ),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
            Surface(
                onClick = { editing = true },
                enabled = enabled && !conversion.loading,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.heightIn(min = 44.dp).testTag("change_rate"),
            ) {
                Box(Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(Res.string.currency_change_rate),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
    if (editing) RateDialog(conversion = conversion, onDismiss = { editing = false }, onSave = {
        onChangeRate(it)
        editing = false
    })
}

@Composable
private fun RateDialog(conversion: ConversionUi, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(conversion.rate.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.currency_rate_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = {
                    Text(stringResource(Res.string.currency_rate_label, conversion.from, conversion.to))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("rate_field"),
            )
        },
        confirmButton = {
            DialogActions(
                confirmText = stringResource(Res.string.form_save),
                dismissText = stringResource(Res.string.ui_cancel),
                confirmEnabled = OriginalAmountDomain.normalizeRate(text) != null,
                confirmModifier = Modifier.testTag("confirm_rate"),
                onConfirm = { onSave(text) },
                onDismiss = onDismiss,
            )
        },
    )
}
