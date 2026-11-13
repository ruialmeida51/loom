package com.eyther.lumbridge.features.shopping.presentation.mapper

import androidx.compose.runtime.Composable
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.token_bakery
import com.eyther.lumbridge.features.shopping.generated.resources.token_dairy_eggs
import com.eyther.lumbridge.features.shopping.generated.resources.token_household
import com.eyther.lumbridge.features.shopping.generated.resources.token_pantry
import com.eyther.lumbridge.features.shopping.generated.resources.token_produce
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun shoppingCategoryLabel(value: String): String =
    when (value) {
        "Produce" -> stringResource(Res.string.token_produce)
        "Bakery" -> stringResource(Res.string.token_bakery)
        "Dairy & eggs" -> stringResource(Res.string.token_dairy_eggs)
        "Pantry" -> stringResource(Res.string.token_pantry)
        "Household" -> stringResource(Res.string.token_household)
        else -> value
    }
