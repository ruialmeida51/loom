package com.eyther.lumbridge.app.share

import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListIconDomain
import com.eyther.lumbridge.features.shopping.presentation.mapper.shoppingListShareText
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class ShoppingListShareTextTest {
    private val original = Locale.getDefault()
    private val list =
        ShoppingListDomain(id = "l", name = "Groceries", icon = ShoppingListIconDomain.CART)

    private fun item(id: String, name: String, quantity: String = "") =
        ShoppingItemDomain(
            id = id,
            name = name,
            quantity = quantity,
            category = "",
            checked = false,
            listId = "l",
        )

    @BeforeTest fun english() = Locale.setDefault(Locale.UK)

    @AfterTest fun restore() = Locale.setDefault(original)

    @Test
    fun theListReadsAsAChecklistWithGroupsIndentedAndLinksWhole() = runTest {
        val text =
            shoppingListShareText(
                list = list,
                items =
                    listOf(
                        item("milk", "Milk", quantity = "2 L"),
                        item("bread", "Bread"),
                        item("rye", "Rye"),
                        item("bulb", "Bulbs https://example.com/bulbs"),
                    ),
                nestedIds = setOf("rye"),
            )
        assertEquals(
            """
            🛒 Groceries
            ━━━━━━━━━━━━
            ☐ Milk · 2 L
            ☐ Bread
              ☐ Rye
            ☐ Bulbs https://example.com/bulbs

            Shared from Loom
            """
                .trimIndent(),
            text,
        )
    }

    @Test
    fun anEmptyListSaysSo() = runTest {
        val text = shoppingListShareText(list = list, items = emptyList(), nestedIds = emptySet())
        assertEquals("🛒 Groceries\n━━━━━━━━━━━━\nNothing left to buy ✨\n\nShared from Loom", text)
    }
}
