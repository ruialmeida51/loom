package com.eyther.lumbridge.features.shopping.presentation.mapper

import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingListIconDomain

internal fun ShoppingListIconDomain.toGlyph(): Glyph =
    when (this) {
        ShoppingListIconDomain.BAG -> Glyph.SHOPPING
        ShoppingListIconDomain.CART -> Glyph.CART
        ShoppingListIconDomain.PRODUCE -> Glyph.LEAF
        ShoppingListIconDomain.HOME -> Glyph.HOME
        ShoppingListIconDomain.GIFT -> Glyph.GIFT
        ShoppingListIconDomain.TRAVEL -> Glyph.PLANE
        ShoppingListIconDomain.PETS -> Glyph.PAW
        ShoppingListIconDomain.HEALTH -> Glyph.HEART
    }
