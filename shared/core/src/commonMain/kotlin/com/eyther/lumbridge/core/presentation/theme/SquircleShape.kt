package com.eyther.lumbridge.core.presentation.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
import kotlin.math.sin

private const val SquircleSteps = 96

/**
 * A superellipse (|x|ⁿ + |y|ⁿ = 1): the smooth "squircle" behind every icon tile, so icons share
 * one silhouette instead of mixing circles and rounded squares. Higher [exponent] is squarer.
 */
class SquircleShape(private val exponent: Double = 4.5) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val halfWidth = size.width / 2
        val halfHeight = size.height / 2
        val power = 2.0 / exponent
        val path = Path()
        for (step in 0..SquircleSteps) {
            val angle = 2 * kotlin.math.PI * step / SquircleSteps
            val c = cos(angle)
            val s = sin(angle)
            val x = halfWidth + halfWidth * (sign(c) * abs(c).pow(power)).toFloat()
            val y = halfHeight + halfHeight * (sign(s) * abs(s).pow(power)).toFloat()
            if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

/** The shared squircle for icon tiles and avatars' icon backgrounds. */
val LoomSquircle: Shape = SquircleShape()
