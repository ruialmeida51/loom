package com.eyther.lumbridge.features.expenses.domain.model

/** The icons a money account can wear, keyed as on the server. */
enum class ExpenseAccountIconDomain(val key: String) {
    HOME("home"),
    TRAVEL("travel"),
    EVENT("event"),
    GIFT("gift"),
    FOOD("food"),
    NATURE("nature"),
    PETS("pets"),
    HEALTH("health");

    companion object {
        fun fromKey(key: String?): ExpenseAccountIconDomain? = entries.firstOrNull { it.key == key }
    }
}
