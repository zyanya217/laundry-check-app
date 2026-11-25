package io.github.zyanya217.laundrycheck.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Icons drawn on a 20 x 20 grid without an icon library.

@Composable
private fun StrokeIcon(size: Dp, color: Color, strokeWidth: Float, modifier: Modifier, draw: DrawScope.(Stroke) -> Unit) {
    Canvas(modifier.size(size)) {
        val k = this.size.minDimension / 20f
        val stroke = Stroke(width = strokeWidth * k, cap = StrokeCap.Round, join = StrokeJoin.Round)
        draw(stroke)
    }
}

private fun DrawScope.line(color: Color, stroke: Stroke, x1: Float, y1: Float, x2: Float, y2: Float) {
    val k = size.minDimension / 20f
    drawLine(color, Offset(x1 * k, y1 * k), Offset(x2 * k, y2 * k), stroke.width, StrokeCap.Round)
}

@Composable
fun ChevronDownIcon(color: Color, modifier: Modifier = Modifier, size: Dp = 14.dp) {
    StrokeIcon(size, color, 1.8f, modifier) { stroke ->
        val k = this.size.minDimension / 20f
        val path = Path().apply {
            moveTo(5f * k, 8f * k)
            lineTo(10f * k, 13f * k)
            lineTo(15f * k, 8f * k)
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
fun CloseIcon(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    StrokeIcon(size, color, 1.8f, modifier) { stroke ->
        line(color, stroke, 5f, 5f, 15f, 15f)
        line(color, stroke, 15f, 5f, 5f, 15f)
    }
}

@Composable
fun SlidersIcon(color: Color, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    StrokeIcon(size, color, 1.6f, modifier) { stroke ->
        val k = this.size.minDimension / 20f
        line(color, stroke, 3f, 6f, 12f, 6f)
        line(color, stroke, 16f, 6f, 17f, 6f)
        line(color, stroke, 3f, 14f, 4f, 14f)
        line(color, stroke, 8f, 14f, 17f, 14f)
        drawCircle(color, radius = 2f * k, center = Offset(14f * k, 6f * k), style = stroke)
        drawCircle(color, radius = 2f * k, center = Offset(6f * k, 14f * k), style = stroke)
    }
}

@Composable
fun LocateIcon(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    StrokeIcon(size, color, 1.6f, modifier) { stroke ->
        val k = this.size.minDimension / 20f
        drawCircle(color, radius = 3.2f * k, center = Offset(10f * k, 10f * k), style = stroke)
        line(color, stroke, 10f, 1.5f, 10f, 4.5f)
        line(color, stroke, 10f, 15.5f, 10f, 18.5f)
        line(color, stroke, 1.5f, 10f, 4.5f, 10f)
        line(color, stroke, 15.5f, 10f, 18.5f, 10f)
    }
}
