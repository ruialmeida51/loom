package com.eyther.lumbridge.features.shopping.presentation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.features.shopping.domain.model.ShoppingItemDomain

@Stable data class ShoppingFavoriteUi(val item: ShoppingItemDomain, val alreadyOnList: Boolean)
