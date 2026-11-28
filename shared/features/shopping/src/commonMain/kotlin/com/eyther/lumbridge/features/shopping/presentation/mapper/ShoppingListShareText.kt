package com.eyther.lumbridge.features.shopping.presentation.mapper

import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListIconDomain
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.share_empty
import com.eyther.lumbridge.features.shopping.generated.resources.share_footer
import org.jetbrains.compose.resources.getString

private const val Rule = "━━━━━━━━━━━━"

// Em spaces survive the trimming chat apps do to leading spaces, so nested items stay indented.
private const val Indent = "  "

/** The emoji that stands for a list's icon in shared text. */
internal fun ShoppingListIconDomain.shareEmoji(): String =
    when (this) {
        ShoppingListIconDomain.BAG -> "🛍️"
        ShoppingListIconDomain.CART -> "🛒"
        ShoppingListIconDomain.PRODUCE -> "🥬"
        ShoppingListIconDomain.HOME -> "🏠"
        ShoppingListIconDomain.GIFT -> "🎁"
        ShoppingListIconDomain.TRAVEL -> "✈️"
        ShoppingListIconDomain.PETS -> "🐾"
        ShoppingListIconDomain.HEALTH -> "💊"
    }

/**
 * [list] as text to send anywhere: what is still to buy, in list order, nested items indented
 * under their group, each with its quantity or note, and links kept whole so they open.
 * [items] are the unchecked items in display order; [nestedIds] are the ones inside a group.
 */
suspend fun shoppingListShareText(
    list: ShoppingListDomain,
    items: List<ShoppingItemDomain>,
    nestedIds: Set<String>,
): String = buildString {
    appendLine("${list.icon.shareEmoji()} ${list.name}")
    appendLine(Rule)
    if (items.isEmpty()) appendLine(getString(Res.string.share_empty))
    items.forEach { item ->
        if (item.id in nestedIds) append(Indent)
        append("☐ ")
        append(item.name.trim())
        item.quantity.trim().takeIf { it.isNotEmpty() }?.let { append(" · $it") }
        appendLine()
    }
    appendLine()
    append(getString(Res.string.share_footer))
}
