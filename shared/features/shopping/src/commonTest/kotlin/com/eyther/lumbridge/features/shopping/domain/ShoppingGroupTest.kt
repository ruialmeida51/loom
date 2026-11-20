package com.eyther.lumbridge.features.shopping.domain

import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListDomain
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingSnapshotDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShoppingGroupTest {
    private val parent = item(id = "produce", position = 1)
    private val apple = item(id = "apple", parentId = parent.id, position = 4)
    private val banana = item(id = "banana", parentId = parent.id, position = 5)
    private val bread = item(id = "bread", position = 3)

    private fun snapshot() =
        ShoppingSnapshotDomain(
            lists = listOf(ShoppingListDomain(id = "list", name = "Groceries")),
            items = listOf(parent, bread, apple, banana),
        )

    @Test
    fun groupsRemainContiguousAndOnlyRootItemsWithoutChildrenCanBeIndented() {
        val snapshot = snapshot()
        assertEquals(
            listOf("produce", "apple", "banana", "bread"),
            snapshot.activeItems.map { it.id },
        )
        assertEquals(parent, snapshot.nestingParent(bread))
        assertNull(snapshot.nestingParent(parent))
        assertNull(snapshot.nestingParent(apple))
    }

    @Test
    fun aLineBelowAnItemTakesTheNextPositionAndJoinsItsGroupLikeTheServer() {
        val snapshot = snapshot()
        val new = item(id = "new", position = 99)
        val belowParent = snapshot.placeBelow(item = new, afterId = parent.id)
        assertEquals(listOf("new", "bread", "apple", "banana"), belowParent.map { it.id })
        assertEquals(2L, belowParent.first().position)
        assertEquals(parent.id, belowParent.first().parentId, "Below a parent it becomes a child")
        assertEquals(listOf(4L, 5L, 6L), belowParent.drop(1).map { it.position })
        assertEquals(parent.id, snapshot.placeBelow(item = new, afterId = apple.id).first().parentId)
        assertNull(snapshot.placeBelow(item = new, afterId = bread.id).first().parentId)
        assertEquals(listOf(new), snapshot.placeBelow(item = new, afterId = "missing"))
    }

    @Test
    fun checkingAndMovingAParentChangesTheWholeFamilyButDeletionKeepsChildren() {
        val snapshot = snapshot()
        assertTrue(snapshot.changeGroup(parent.copy(checked = true)).all { it.checked })
        assertTrue(snapshot.changeGroup(parent.copy(listId = "other")).all { it.listId == "other" })
        val removed = snapshot.changeGroup(parent.copy(removed = true))
        assertFalse(removed.first { it.id == "apple" }.removed)
        val active = snapshot.copy(items = removed + bread).activeItems
        assertEquals(setOf("bread", "apple", "banana"), active.map { it.id }.toSet())
    }

    @Test
    fun completingTheLastChildCompletesTheParentAndUncheckingOneReopensIt() {
        val checked = snapshot().copy(items = listOf(parent, apple.copy(checked = true), banana))
        assertTrue(checked.changeGroup(banana.copy(checked = true)).all { it.checked })
        val complete =
            snapshot()
                .copy(
                    items =
                        listOf(
                            parent.copy(checked = true),
                            apple.copy(checked = true),
                            banana.copy(checked = true),
                        ),
                )
        val reopened = complete.changeGroup(apple.copy(checked = false))
        assertFalse(reopened.first { it.id == parent.id }.checked)
        assertTrue(reopened.first { it.id == banana.id }.checked)
    }

    private fun item(id: String, parentId: String? = null, position: Long) =
        ShoppingItemDomain(
            id = id,
            name = id,
            quantity = "",
            category = "Other",
            checked = false,
            listId = "list",
            parentId = parentId,
            version = 1,
            position = position,
        )
}
