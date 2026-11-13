package com.eyther.lumbridge.features.shopping.domain.model

data class ShoppingListDomain(
    val id: String,
    val name: String,
    val archived: Boolean = false,
    val version: Long = 0,
) {
    fun validated(): ShoppingListDomain {
        require(name.trim().length in 1..60) {
            "shopping.message_give_the_list_a_name_up_to_60_characters"
        }
        return copy(name = name.trim())
    }
}
