package com.eyther.lumbridge.features.shopping.domain

import com.eyther.lumbridge.features.shopping.domain.model.*
import kotlin.test.*

class ShoppingPolicyTest {
    @Test
    fun archivedAndRemovedItemsDoNotAppearOnActiveLists() {
        val snapshot =
            ShoppingSnapshotDomain(
                listOf(
                    ShoppingListDomain("food", "Groceries"),
                    ShoppingListDomain("old", "Old", true),
                ),
                listOf(
                    item("milk", "Milk", "food"),
                    item("bread", "Bread", "food").copy(removed = true),
                    item("paint", "Paint", "old"),
                ),
            )
        assertEquals(listOf("Milk"), snapshot.activeItems.map { it.name })
        assertEquals(listOf("food"), snapshot.activeLists.map { it.id })
    }

    @Test
    fun suggestionsReuseFavoritesWithoutDuplicatingOutstandingItems() {
        val snapshot =
            ShoppingSnapshotDomain(
                listOf(ShoppingListDomain("food", "Groceries")),
                listOf(
                    item("a", "Milk", "food"),
                    item("b", "milk", "other").copy(checked = true, favorite = true),
                    item("c", "Coffee", "food").copy(checked = true, favorite = true),
                    item("d", "Bread", "food").copy(removed = true),
                ),
            )
        assertEquals(listOf("Coffee", "Bread"), snapshot.suggestions("food").map { it.name })
    }

    @Test
    fun invalidInputsCannotReachTheDataBoundary() {
        assertFailsWith<IllegalArgumentException> { item("a", " ", "food").validated() }
        assertFailsWith<IllegalArgumentException> { item("a", "Milk", "").validated() }
        assertFailsWith<IllegalArgumentException> {
            ShoppingListDomain("list", "x".repeat(61)).validated()
        }
        assertEquals("Milk", item("a", " Milk ", "food").validated().name)
    }

    private fun item(id: String, name: String, list: String) =
        ShoppingItemDomain(id, name, "", "Pantry", false, list)
}
