package com.eyther.lumbridge.features.shopping.domain.model

data class ShoppingSnapshotDomain(
    val lists: List<ShoppingListDomain> = emptyList(),
    val items: List<ShoppingItemDomain> = emptyList(),
) {

    val activeLists
        get() = lists.filterNot { it.archived }.sortedBy { it.name.lowercase() }

    val activeItems
        get() = items.filter { item -> !item.removed && activeLists.any { it.id == item.listId } }

    fun resolveActiveListId(preferredId: String): String =
        activeLists.firstOrNull { it.id == preferredId }?.id
            ?: activeLists.firstOrNull { it.name == "Groceries" }?.id
            ?: activeLists.firstOrNull()?.id.orEmpty()

    fun findReusableItem(listId: String, name: String): ShoppingItemDomain? =
        items.firstOrNull { it.listId == listId && it.name.equals(name, ignoreCase = true) }

    fun suggestions(listId: String): List<ShoppingItemDomain> =
        items
            .filter { it.favorite || it.checked || it.removed }
            .filter { candidate ->
                activeItems.none {
                    it.listId == listId && !it.checked && it.name.equals(candidate.name, true)
                }
            }
            .sortedWith(
                compareByDescending<ShoppingItemDomain> { it.favorite }
                    .thenBy { it.name.lowercase() },
            )
            .distinctBy { it.name.trim().lowercase() }
            .take(8)
}
