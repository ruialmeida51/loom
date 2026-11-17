package com.eyther.lumbridge.core.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.eyther.lumbridge.core.generated.resources.Inter_400
import com.eyther.lumbridge.core.generated.resources.Inter_500
import com.eyther.lumbridge.core.generated.resources.Inter_600
import com.eyther.lumbridge.core.generated.resources.Inter_700
import com.eyther.lumbridge.core.generated.resources.Res
import com.eyther.lumbridge.core.generated.resources.SpaceGrotesk_600
import com.eyther.lumbridge.core.generated.resources.SpaceGrotesk_700
import org.jetbrains.compose.resources.Font

/** Plannot's licensed font pair, mapped onto one shared Material type scale. */
@Composable
internal fun loomTypography(): Typography {
    val body =
        FontFamily(
            Font(Res.font.Inter_400, weight = FontWeight.Normal),
            Font(Res.font.Inter_500, weight = FontWeight.Medium),
            Font(Res.font.Inter_600, weight = FontWeight.SemiBold),
            Font(Res.font.Inter_700, weight = FontWeight.Bold),
        )
    val heading =
        FontFamily(
            Font(Res.font.SpaceGrotesk_600, weight = FontWeight.SemiBold),
            Font(Res.font.SpaceGrotesk_700, weight = FontWeight.Bold),
        )
    return remember(body, heading) {
        fun style(
            size: Int,
            lineHeight: Int,
            weight: FontWeight,
            family: FontFamily = body,
        ): TextStyle =
            TextStyle(
                fontFamily = family,
                fontSize = size.sp,
                lineHeight = lineHeight.sp,
                fontWeight = weight,
                letterSpacing = 0.sp,
                fontFeatureSettings = "tnum",
            )
        Typography(
            displayLarge = style(36, 44, FontWeight.SemiBold, heading),
            displayMedium = style(32, 40, FontWeight.SemiBold, heading),
            displaySmall = style(28, 36, FontWeight.SemiBold, heading),
            headlineLarge = style(28, 36, FontWeight.SemiBold, heading),
            headlineMedium = style(24, 32, FontWeight.SemiBold, heading),
            headlineSmall = style(22, 28, FontWeight.SemiBold, heading),
            titleLarge = style(20, 26, FontWeight.SemiBold, heading),
            titleMedium = style(16, 22, FontWeight.SemiBold),
            titleSmall = style(14, 20, FontWeight.SemiBold),
            bodyLarge = style(15, 22, FontWeight.Normal),
            bodyMedium = style(14, 20, FontWeight.Normal),
            bodySmall = style(12, 18, FontWeight.Normal),
            labelLarge = style(14, 20, FontWeight.Medium),
            labelMedium = style(12, 16, FontWeight.Medium),
            labelSmall = style(11, 16, FontWeight.Medium),
        )
    }
}
