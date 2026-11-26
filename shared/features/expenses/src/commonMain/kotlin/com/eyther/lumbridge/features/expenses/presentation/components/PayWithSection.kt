package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoomDivider
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.pay_copied
import com.eyther.lumbridge.features.expenses.generated.resources.pay_hint
import com.eyther.lumbridge.features.expenses.generated.resources.pay_mbway
import com.eyther.lumbridge.features.expenses.generated.resources.pay_mbway_copied
import com.eyther.lumbridge.features.expenses.generated.resources.pay_open
import com.eyther.lumbridge.features.expenses.generated.resources.pay_paypal
import com.eyther.lumbridge.features.expenses.generated.resources.pay_paypal_amount
import com.eyther.lumbridge.features.expenses.generated.resources.pay_revolut
import com.eyther.lumbridge.features.expenses.generated.resources.pay_revolut_hint
import com.eyther.lumbridge.features.expenses.generated.resources.pay_title
import com.eyther.lumbridge.features.expenses.presentation.model.PayOptionsUi
import org.jetbrains.compose.resources.stringResource

private const val MbWayAppLink = "mbway://"

/** "+351912345678" as "+351 912 345 678", the way it is read aloud and typed. */
private fun formatPhone(phone: String): String =
    if (phone.startsWith("+351") && phone.length == 13)
        "+351 ${phone.substring(4, 7)} ${phone.substring(7, 10)} ${phone.substring(10)}"
    else phone

/**
 * Sends the money in another app, then the person comes back and saves to record it: PayPal opens
 * with the amount filled in, Revolut opens the profile, and MB Way, which has no link that fills
 * anything in, copies the number and opens the app.
 */
@Composable
internal fun PayWithSection(options: PayOptionsUi, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf<String?>(null) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
        Text(
            stringResource(Res.string.pay_title, options.recipientName),
            style = MaterialTheme.typography.titleSmall,
        )
        GroupedSurface {
            val rows =
                listOfNotNull(
                    options.paypalLink?.let { link ->
                        @Composable {
                            NavigationRow(
                                title = stringResource(Res.string.pay_paypal),
                                description =
                                    stringResource(Res.string.pay_paypal_amount, options.amountLabel),
                                glyph = Glyph.MONEY,
                                onClick = { uriHandler.openUri(link) },
                                modifier = Modifier.testTag("pay_paypal"),
                            )
                        }
                    },
                    options.revolutLink?.let { link ->
                        @Composable {
                            NavigationRow(
                                title = stringResource(Res.string.pay_revolut),
                                description = stringResource(Res.string.pay_revolut_hint),
                                glyph = Glyph.MONEY,
                                onClick = {
                                    options.amount?.let {
                                        clipboard.setText(AnnotatedString(it))
                                        copied = "amount"
                                    }
                                    uriHandler.openUri(link)
                                },
                                modifier = Modifier.testTag("pay_revolut"),
                            )
                        }
                    },
                    options.handles.mbwayPhone?.let { phone ->
                        @Composable {
                            NavigationRow(
                                title = stringResource(Res.string.pay_mbway),
                                description =
                                    if (copied == "mbway")
                                        stringResource(
                                            Res.string.pay_mbway_copied,
                                            options.amountLabel,
                                        )
                                    else formatPhone(phone),
                                glyph = Glyph.MONEY,
                                onClick = {
                                    // MB Way offers no link that fills in someone and an amount, so
                                    // copy the number and open the app at its home.
                                    clipboard.setText(AnnotatedString(phone))
                                    copied = "mbway"
                                    runCatching { uriHandler.openUri(MbWayAppLink) }
                                },
                                modifier = Modifier.testTag("pay_mbway"),
                                trailingContent = {
                                    Text(
                                        stringResource(
                                            if (copied == "mbway") Res.string.pay_copied
                                            else Res.string.pay_open,
                                        ),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                },
                            )
                        }
                    },
                )
            rows.forEachIndexed { index, row ->
                if (index > 0) LoomDivider()
                row()
            }
        }
        Text(
            stringResource(Res.string.pay_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
