package com.eyther.lumbridge.features.shopping.domain.model

/** The icon a household picks for a list. [key] is the stored value; unknown keys fall back. */
enum class ShoppingListIconDomain(val key: String) {
    BAG("bag"),
    CART("cart"),
    PRODUCE("produce"),
    HOME("home"),
    GIFT("gift"),
    TRAVEL("travel"),
    PETS("pets"),
    HEALTH("health");

    companion object {
        fun fromKey(key: String?): ShoppingListIconDomain = entries.firstOrNull { it.key == key } ?: BAG
    }
}
