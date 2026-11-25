package com.eyther.lumbridge.core.presentation.components.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.generated.resources.Res
import com.eyther.lumbridge.core.generated.resources.icon_add
import com.eyther.lumbridge.core.generated.resources.icon_edit
import com.eyther.lumbridge.core.generated.resources.icon_health
import com.eyther.lumbridge.core.generated.resources.icon_pets
import com.eyther.lumbridge.core.generated.resources.icon_travel
import com.eyther.lumbridge.core.generated.resources.icon_gift
import com.eyther.lumbridge.core.generated.resources.icon_produce
import com.eyther.lumbridge.core.generated.resources.icon_cart
import com.eyther.lumbridge.core.generated.resources.icon_bell
import com.eyther.lumbridge.core.generated.resources.icon_calendar
import com.eyther.lumbridge.core.generated.resources.icon_delete
import com.eyther.lumbridge.core.generated.resources.icon_done
import com.eyther.lumbridge.core.generated.resources.icon_expand
import com.eyther.lumbridge.core.generated.resources.icon_feed
import com.eyther.lumbridge.core.generated.resources.icon_home
import com.eyther.lumbridge.core.generated.resources.icon_log_out
import com.eyther.lumbridge.core.generated.resources.icon_money
import com.eyther.lumbridge.core.generated.resources.icon_more
import com.eyther.lumbridge.core.generated.resources.icon_next
import com.eyther.lumbridge.core.generated.resources.icon_person
import com.eyther.lumbridge.core.generated.resources.icon_photo
import com.eyther.lumbridge.core.generated.resources.icon_privacy
import com.eyther.lumbridge.core.generated.resources.icon_settings
import com.eyther.lumbridge.core.generated.resources.icon_shopping
import com.eyther.lumbridge.core.generated.resources.icon_star
import com.eyther.lumbridge.core.generated.resources.icon_tasks
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoomIcon(
    glyph: Glyph,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    decorative: Boolean = false,
) {
    val iconLabel =
        stringResource(
            when (glyph) {
                Glyph.MORE -> Res.string.icon_more
                Glyph.HOME -> Res.string.icon_home
                Glyph.FEED -> Res.string.icon_feed
                Glyph.PHOTO -> Res.string.icon_photo
                Glyph.CALENDAR -> Res.string.icon_calendar
                Glyph.SHOPPING -> Res.string.icon_shopping
                Glyph.MONEY -> Res.string.icon_money
                Glyph.BELL -> Res.string.icon_bell
                Glyph.TASKS -> Res.string.icon_tasks
                Glyph.SETTINGS -> Res.string.icon_settings
                Glyph.PERSON -> Res.string.icon_person
                Glyph.SHIELD -> Res.string.icon_privacy
                Glyph.PLUS -> Res.string.icon_add
                Glyph.ARROW -> Res.string.icon_next
                Glyph.CHECK -> Res.string.icon_done
                Glyph.STAR -> Res.string.icon_star
                Glyph.DELETE -> Res.string.icon_delete
                Glyph.LOG_OUT -> Res.string.icon_log_out
                Glyph.CHEVRON_DOWN -> Res.string.icon_expand
                Glyph.CHEVRON_RIGHT -> Res.string.icon_next
                Glyph.CART -> Res.string.icon_cart
                Glyph.LEAF -> Res.string.icon_produce
                Glyph.GIFT -> Res.string.icon_gift
                Glyph.PLANE -> Res.string.icon_travel
                Glyph.PAW -> Res.string.icon_pets
                Glyph.HEART -> Res.string.icon_health
                Glyph.EDIT -> Res.string.icon_edit
            },
        )
    Canvas(
        modifier
            .size(23.dp)
            .then(
                if (decorative) Modifier
                else
                    Modifier.semantics {
                        contentDescription = iconLabel
                    },
            ),
    ) {
        val s = size.width / 24f
        fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
            drawLine(
                tint,
                Offset(x1 * s, y1 * s),
                Offset(x2 * s, y2 * s),
                1.7f * s,
                StrokeCap.Round,
            )
        when (glyph) {
            Glyph.STAR -> {
                val star =
                    Path().apply {
                        moveTo(12f * s, 3f * s)
                        lineTo(14.8f * s, 8.7f * s)
                        lineTo(21f * s, 9.6f * s)
                        lineTo(16.5f * s, 14f * s)
                        lineTo(17.6f * s, 20.2f * s)
                        lineTo(12f * s, 17.2f * s)
                        lineTo(6.4f * s, 20.2f * s)
                        lineTo(7.5f * s, 14f * s)
                        lineTo(3f * s, 9.6f * s)
                        lineTo(9.2f * s, 8.7f * s)
                        close()
                    }
                drawPath(star, tint, style = Stroke(1.7f * s))
            }
            Glyph.PERSON -> {
                drawCircle(
                    tint,
                    radius = 4f * s,
                    center = Offset(12f * s, 7f * s),
                    style = Stroke(1.7f * s),
                )
                drawArc(
                    tint,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(4f * s, 14f * s),
                    size = Size(16f * s, 14f * s),
                    style = Stroke(1.7f * s),
                )
            }
            Glyph.SHIELD -> {
                val outline =
                    Path().apply {
                        moveTo(12f * s, 3f * s)
                        lineTo(20f * s, 6f * s)
                        lineTo(19f * s, 14f * s)
                        quadraticTo(17f * s, 19f * s, 12f * s, 22f * s)
                        quadraticTo(7f * s, 19f * s, 5f * s, 14f * s)
                        lineTo(4f * s, 6f * s)
                        close()
                    }
                drawPath(outline, tint, style = Stroke(1.7f * s))
            }
            Glyph.CHEVRON_DOWN -> {
                line(7f, 10f, 12f, 15f)
                line(12f, 15f, 17f, 10f)
            }
            Glyph.EDIT -> {
                // A pencil: barrel, tip and the eraser band.
                line(4f, 20f, 5f, 15f)
                line(4f, 20f, 9f, 19f)
                line(5f, 15f, 16f, 4f)
                line(9f, 19f, 20f, 8f)
                line(16f, 4f, 20f, 8f)
                line(13f, 7f, 17f, 11f)
            }
            Glyph.CHEVRON_RIGHT -> {
                line(10f, 7f, 15f, 12f)
                line(15f, 12f, 10f, 17f)
            }
            Glyph.LOG_OUT -> {
                line(10f, 4f, 4f, 4f)
                line(4f, 4f, 4f, 20f)
                line(4f, 20f, 10f, 20f)
                line(10f, 12f, 21f, 12f)
                line(17f, 8f, 21f, 12f)
                line(21f, 12f, 17f, 16f)
            }
            Glyph.DELETE -> {
                line(5f, 6f, 19f, 6f)
                line(9f, 3f, 15f, 3f)
                line(7f, 9f, 8f, 21f)
                line(8f, 21f, 16f, 21f)
                line(16f, 21f, 17f, 9f)
                line(10f, 10f, 10f, 17f)
                line(14f, 10f, 14f, 17f)
            }
            Glyph.MORE ->
                listOf(5f, 12f, 19f).forEach { x ->
                    drawCircle(tint, 1.5f * s, Offset(x * s, 12f * s))
                }
            Glyph.HOME -> {
                line(3f, 11f, 12f, 3f)
                line(12f, 3f, 21f, 11f)
                line(5f, 10f, 5f, 21f)
                line(5f, 21f, 19f, 21f)
                line(19f, 21f, 19f, 10f)
                line(10f, 21f, 10f, 14f)
                line(10f, 14f, 14f, 14f)
                line(14f, 14f, 14f, 21f)
            }
            Glyph.SHOPPING -> {
                drawRoundRect(
                    tint,
                    Offset(4 * s, 7 * s),
                    Size(16 * s, 14 * s),
                    androidx.compose.ui.geometry.CornerRadius(2 * s),
                    style = Stroke(1.7f * s),
                )
                drawArc(
                    tint,
                    180f,
                    180f,
                    false,
                    Offset(8 * s, 2 * s),
                    Size(8 * s, 10 * s),
                    style = Stroke(1.7f * s),
                )
            }
            Glyph.CALENDAR -> {
                drawRoundRect(
                    tint,
                    Offset(3 * s, 5 * s),
                    Size(18 * s, 16 * s),
                    androidx.compose.ui.geometry.CornerRadius(2 * s),
                    style = Stroke(1.7f * s),
                )
                line(3f, 10f, 21f, 10f)
                line(8f, 3f, 8f, 7f)
                line(16f, 3f, 16f, 7f)
                line(7f, 14f, 10f, 14f)
                line(14f, 14f, 17f, 14f)
                line(7f, 18f, 10f, 18f)
            }
            Glyph.PHOTO -> {
                drawRoundRect(
                    tint,
                    Offset(3 * s, 3 * s),
                    Size(18 * s, 18 * s),
                    androidx.compose.ui.geometry.CornerRadius(2 * s),
                    style = Stroke(1.7f * s),
                )
                drawCircle(tint, 1.5f * s, Offset(8 * s, 8 * s))
                line(4f, 18f, 10f, 12f)
                line(10f, 12f, 14f, 16f)
                line(14f, 16f, 17f, 13f)
                line(17f, 13f, 21f, 17f)
            }
            Glyph.FEED -> {
                drawCircle(tint, 1.5f * s, Offset(5 * s, 19 * s))
                drawArc(
                    tint,
                    -90f,
                    90f,
                    false,
                    Offset(-3 * s, 11 * s),
                    Size(16 * s, 16 * s),
                    style = Stroke(1.7f * s),
                )
                drawArc(
                    tint,
                    -90f,
                    90f,
                    false,
                    Offset(-9 * s, 5 * s),
                    Size(28 * s, 28 * s),
                    style = Stroke(1.7f * s),
                )
            }
            Glyph.MONEY -> {
                drawRoundRect(
                    tint,
                    Offset(3 * s, 5 * s),
                    Size(18 * s, 15 * s),
                    androidx.compose.ui.geometry.CornerRadius(3 * s),
                    style = Stroke(1.7f * s),
                )
                line(3f, 10f, 21f, 10f)
                line(15f, 15f, 18f, 15f)
            }
            Glyph.TASKS -> {
                listOf(6f, 12f, 18f).forEach { y ->
                    line(3f, y, 5f, y + 2f)
                    line(5f, y + 2f, 8f, y - 2f)
                    line(11f, y, 21f, y)
                }
            }
            Glyph.BELL -> {
                val p =
                    Path().apply {
                        moveTo(5 * s, 17 * s)
                        lineTo(7 * s, 14 * s)
                        lineTo(7 * s, 9 * s)
                        cubicTo(7 * s, 2 * s, 17 * s, 2 * s, 17 * s, 9 * s)
                        lineTo(17 * s, 14 * s)
                        lineTo(19 * s, 17 * s)
                        close()
                    }
                drawPath(p, tint, style = Stroke(1.7f * s))
                drawArc(
                    tint,
                    0f,
                    180f,
                    false,
                    Offset(10 * s, 18 * s),
                    Size(4 * s, 4 * s),
                    style = Stroke(1.7f * s),
                )
            }
            Glyph.SETTINGS -> {
                drawCircle(tint, 7.5f * s, style = Stroke(1.7f * s))
                drawCircle(tint, 2.5f * s, style = Stroke(1.7f * s))
                line(12f, 2f, 12f, 5f)
                line(12f, 19f, 12f, 22f)
                line(2f, 12f, 5f, 12f)
                line(19f, 12f, 22f, 12f)
            }
            Glyph.PLUS -> {
                line(12f, 5f, 12f, 19f)
                line(5f, 12f, 19f, 12f)
            }
            Glyph.ARROW -> {
                line(5f, 12f, 19f, 12f)
                line(14f, 7f, 19f, 12f)
                line(19f, 12f, 14f, 17f)
            }
            Glyph.CHECK -> {
                line(5f, 12f, 10f, 17f)
                line(10f, 17f, 19f, 7f)
            }
            Glyph.CART -> {
                line(2f, 4f, 5f, 4f)
                line(5f, 4f, 7.5f, 15f)
                line(7.5f, 15f, 18f, 15f)
                line(18f, 15f, 20.5f, 7f)
                line(20.5f, 7f, 6f, 7f)
                drawCircle(tint, 1.5f * s, Offset(9 * s, 19.5f * s))
                drawCircle(tint, 1.5f * s, Offset(17 * s, 19.5f * s))
            }
            Glyph.LEAF -> {
                val leaf =
                    Path().apply {
                        moveTo(5 * s, 19 * s)
                        cubicTo(4 * s, 10 * s, 10 * s, 4 * s, 20 * s, 4 * s)
                        cubicTo(20 * s, 14 * s, 14 * s, 20 * s, 5 * s, 19 * s)
                        close()
                    }
                drawPath(leaf, tint, style = Stroke(1.7f * s))
                line(5f, 19f, 14f, 10f)
            }
            Glyph.GIFT -> {
                drawRoundRect(
                    tint,
                    Offset(4 * s, 10 * s),
                    Size(16 * s, 11 * s),
                    androidx.compose.ui.geometry.CornerRadius(1.5f * s),
                    style = Stroke(1.7f * s),
                )
                line(3f, 7f, 21f, 7f)
                line(3f, 7f, 3f, 10f)
                line(21f, 7f, 21f, 10f)
                line(3f, 10f, 21f, 10f)
                line(12f, 7f, 12f, 21f)
                drawArc(tint, 180f, 180f, false, Offset(7 * s, 3 * s), Size(5 * s, 8 * s), style = Stroke(1.7f * s))
                drawArc(tint, 180f, 180f, false, Offset(12 * s, 3 * s), Size(5 * s, 8 * s), style = Stroke(1.7f * s))
            }
            Glyph.PLANE -> {
                val plane =
                    Path().apply {
                        moveTo(2 * s, 13 * s)
                        lineTo(21 * s, 5 * s)
                        lineTo(16 * s, 20 * s)
                        lineTo(11 * s, 14 * s)
                        close()
                    }
                drawPath(plane, tint, style = Stroke(1.7f * s))
                line(11f, 14f, 21f, 5f)
            }
            Glyph.PAW -> {
                drawCircle(tint, 1.8f * s, Offset(6 * s, 10 * s), style = Stroke(1.7f * s))
                drawCircle(tint, 1.8f * s, Offset(10 * s, 6 * s), style = Stroke(1.7f * s))
                drawCircle(tint, 1.8f * s, Offset(14 * s, 6 * s), style = Stroke(1.7f * s))
                drawCircle(tint, 1.8f * s, Offset(18 * s, 10 * s), style = Stroke(1.7f * s))
                drawOval(tint, Offset(7.5f * s, 12 * s), Size(9 * s, 8 * s), style = Stroke(1.7f * s))
            }
            Glyph.HEART -> {
                val heart =
                    Path().apply {
                        moveTo(12 * s, 20 * s)
                        cubicTo(4 * s, 14 * s, 2 * s, 10 * s, 4 * s, 7 * s)
                        cubicTo(6 * s, 4 * s, 10 * s, 4 * s, 12 * s, 8 * s)
                        cubicTo(14 * s, 4 * s, 18 * s, 4 * s, 20 * s, 7 * s)
                        cubicTo(22 * s, 10 * s, 20 * s, 14 * s, 12 * s, 20 * s)
                        close()
                    }
                drawPath(heart, tint, style = Stroke(1.7f * s))
            }
        }
    }
}
