package com.eyther.lumbridge.features.shopping.domain.model

data class ShoppingItemDomain(
    val id: String,
    val name: String,
    val quantity: String,
    val category: String,
    val checked: Boolean,
    val listId: String,
    val version: Long = 0,
    val removed: Boolean = false,
    val favorite: Boolean = false,
) {
    fun validated(): ShoppingItemDomain {
        require(name.trim().length in 1..120) {
            "shopping.message_give_the_item_a_name_up_to_120_characters"
        }
        require(quantity.length <= 40) { "shopping.message_keep_the_quantity_under_40_characters" }
        require(category.length in 1..40 && listId.isNotBlank()) {
            "shopping.message_choose_a_list_and_category"
        }
        return copy(name = name.trim(), quantity = quantity.trim())
    }
}
