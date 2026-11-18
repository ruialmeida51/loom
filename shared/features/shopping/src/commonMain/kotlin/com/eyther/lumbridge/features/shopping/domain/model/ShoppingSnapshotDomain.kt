package com.eyther.lumbridge.features.shopping.domain.model

data class ShoppingSnapshotDomain(
    val lists: List<ShoppingListDomain> = emptyList(),
    val items: List<ShoppingItemDomain> = emptyList(),
) {

    val activeLists: List<ShoppingListDomain> =
        lists.filterNot { it.archived }.sortedBy { it.name.lowercase() }
    private val activeListIds: Set<String> = activeLists.map { it.id }.toSet()
    val activeItems: List<ShoppingItemDomain> = run {
        val active =
            items.filter { !it.removed && it.listId in activeListIds }.sortedBy { it.position }
        val ids = active.map { it.id }.toSet()
        val children = active.groupBy { it.parentId }
        active.filter { it.parentId !in ids }.flatMap { listOf(it) + children[it.id].orEmpty() }
    }

    fun groupFor(item: ShoppingItemDomain): List<ShoppingItemDomain> {
        val rootId = item.parentId ?: item.id
        return items.filter {
            it.id == rootId || it.id == item.id || (it.parentId == rootId && !it.removed)
        }
    }

    private val nestingParentsByList: Map<String, Map<String, String>> = run {
        val parentsWithChildren = items.filterNot { it.removed }.mapNotNull { it.parentId }.toSet()
        activeItems
            .groupBy { it.listId }
            .mapValues { (_, list) ->
                var previousRoot: ShoppingItemDomain? = null
                buildMap {
                    list.forEach { item ->
                        if (item.parentId == null && !item.checked) {
                            val previous = previousRoot
                            if (previous != null && item.id !in parentsWithChildren)
                                put(item.id, previous.id)
                            previousRoot = item
                        }
                    }
                }
            }
    }

    fun nestingParents(listId: String): Map<String, String> = nestingParentsByList[listId].orEmpty()

    fun nestingParent(item: ShoppingItemDomain): ShoppingItemDomain? =
        nestingParents(item.listId)[item.id]?.let { parentId ->
            items.firstOrNull { it.id == parentId }
        }

    /** Preserve the complete family so the server can validate every version before changing it. */
    fun changeGroup(item: ShoppingItemDomain): List<ShoppingItemDomain> {
        val before = items.first { it.id == item.id }
        val affected =
            (groupFor(before) +
                    item.parentId
                        ?.let { id -> items.firstOrNull { it.id == id }?.let(::groupFor) }
                        .orEmpty())
                .distinctBy { it.id }
        var changed = affected.map { row ->
            when {
                row.id == item.id -> item
                before.parentId == null &&
                    row.parentId == before.id &&
                    item.removed &&
                    !before.removed -> row.copy(parentId = null)
                before.parentId == null &&
                    row.parentId == before.id &&
                    item.listId != before.listId -> row.copy(listId = item.listId)
                before.parentId == null &&
                    row.parentId == before.id &&
                    item.checked != before.checked -> row.copy(checked = item.checked)
                else -> row
            }
        }
        val parents = setOfNotNull(before.parentId, item.parentId)
        parents.forEach { parentId ->
            val children = changed.filter { it.parentId == parentId && !it.removed }
            if (children.isNotEmpty())
                changed = changed.map {
                    if (it.id == parentId)
                        it.copy(checked = children.all { child -> child.checked })
                    else it
                }
        }
        return changed
    }

    fun resolveActiveListId(preferredId: String): String =
        activeLists.firstOrNull { it.id == preferredId }?.id
            ?: activeLists.firstOrNull { it.name == "Groceries" }?.id
            ?: activeLists.firstOrNull()?.id.orEmpty()

    fun findReusableItem(listId: String, name: String): ShoppingItemDomain? = items.firstOrNull {
        it.listId == listId && it.name.equals(name, ignoreCase = true)
    }

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
