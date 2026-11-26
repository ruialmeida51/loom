package com.eyther.lumbridge.features.expenses.presentation.mapper

import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountIconDomain

// Glyphs are a presentation type the domain cannot see, so the mapping lives here.
internal fun ExpenseAccountIconDomain.toGlyph(): Glyph =
    when (this) {
        ExpenseAccountIconDomain.HOME -> Glyph.HOME
        ExpenseAccountIconDomain.TRAVEL -> Glyph.PLANE
        ExpenseAccountIconDomain.EVENT -> Glyph.CALENDAR
        ExpenseAccountIconDomain.GIFT -> Glyph.GIFT
        ExpenseAccountIconDomain.FOOD -> Glyph.CART
        ExpenseAccountIconDomain.NATURE -> Glyph.LEAF
        ExpenseAccountIconDomain.PETS -> Glyph.PAW
        ExpenseAccountIconDomain.HEALTH -> Glyph.HEART
    }

internal fun ExpenseAccountDomain.glyph(): Glyph = displayIcon.toGlyph()
