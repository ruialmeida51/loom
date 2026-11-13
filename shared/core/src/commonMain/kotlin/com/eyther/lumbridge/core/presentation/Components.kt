package com.eyther.lumbridge.core.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.eyther.lumbridge.core.generated.resources.*
import org.jetbrains.compose.resources.stringResource

enum class Glyph {
    HOME,
    FEED,
    SHOPPING,
    MONEY,
    BELL,
    SETTINGS,
    PLUS,
    ARROW,
    CHECK,
}

@Composable
fun LoomIcon(
    glyph: Glyph,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    val iconLabel =
        stringResource(
            when (glyph) {
                Glyph.HOME -> Res.string.icon_home
                Glyph.FEED -> Res.string.icon_feed
                Glyph.SHOPPING -> Res.string.icon_shopping
                Glyph.MONEY -> Res.string.icon_money
                Glyph.BELL -> Res.string.icon_bell
                Glyph.SETTINGS -> Res.string.icon_settings
                Glyph.PLUS -> Res.string.icon_add
                Glyph.ARROW -> Res.string.icon_next
                Glyph.CHECK -> Res.string.icon_done
            },
        )
    Canvas(
        modifier.size(23.dp).semantics {
            contentDescription = iconLabel
        },
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
        }
    }
}

@Composable
fun LoomCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun PageHeading(
    eyebrow: String,
    title: String,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            eyebrow.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.headlineLarge)
            action?.invoke()
        }
        if (subtitle != null)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
    }
}

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (action != null)
            TextButton(onClick = onAction) {
                Text(action)
                Spacer(Modifier.width(5.dp))
                LoomIcon(Glyph.ARROW, Modifier.size(15.dp), MaterialTheme.colorScheme.primary)
            }
    }
}

@Composable
fun ErrorNotice(message: String?, onDismiss: () -> Unit) {
    if (message != null)
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    message,
                    Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.ui_dismiss)) }
            }
        }
}

@Composable
fun EmptyNotice(title: String, description: String) {
    LoomCard {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun LoomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 50.dp),
        enabled = enabled && !loading,
        shape = RoundedCornerShape(16.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(text)
    }
}
