package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingNamePartDomain
import kotlinx.collections.immutable.ImmutableList

/**
 * An item's name with each link shown as its site. Tapping a link opens it in the browser (through
 * the platform's URI handler); a tap anywhere else falls through to the row, which edits the name.
 */
@Composable
internal fun ShoppingItemName(
    parts: ImmutableList<ShoppingNamePartDomain>,
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val text =
        remember(parts, linkColor) {
            buildAnnotatedString {
                parts.forEach { part ->
                    when (part) {
                        is ShoppingNamePartDomain.Text -> append(part.value)
                        is ShoppingNamePartDomain.Link ->
                            withLink(
                                LinkAnnotation.Url(
                                    url = part.url,
                                    styles =
                                        TextLinkStyles(
                                            style =
                                                SpanStyle(
                                                    color = linkColor,
                                                    fontWeight = FontWeight.Medium,
                                                    textDecoration = TextDecoration.Underline,
                                                ),
                                        ),
                                ),
                            ) {
                                append(part.label)
                            }
                    }
                }
            }
        }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
        modifier = modifier,
    )
}
