package com.testmynoetic.prep.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testmynoetic.core.Figure
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/** Draws a question's picture from its data description. */
@Composable
fun FigureView(figure: Figure) {
    // On a TV the question sits beside the keypad, so keep pictures short enough to fit without scrolling.
    val fit = if (LocalWide.current) Modifier.heightIn(max = 240.dp) else Modifier
    Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).then(fit), contentAlignment = Alignment.Center) {
        when (figure) {
            is Figure.TextArt -> TextArtFigure(figure)
            is Figure.Pyramid -> PyramidFigure(figure)
            is Figure.Venn -> VennFigure(figure)
            is Figure.Grid -> GridFigure(figure)
            is Figure.Clock -> ClockFigure(figure)
            is Figure.Balance -> BalanceFigure(figure)
            is Figure.BarChart -> BarChartFigure(figure)
            is Figure.Table -> TableFigure(figure)
            is Figure.Shape -> ShapeFigure(figure)
        }
    }
}

private fun DrawScope.centeredText(measurer: TextMeasurer, text: String, center: Offset, style: TextStyle) {
    val layout = measurer.measure(text, style)
    drawText(layout, topLeft = Offset(center.x - layout.size.width / 2f, center.y - layout.size.height / 2f))
}

@Composable
private fun TextArtFigure(f: Figure.TextArt) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            f.lines.forEach {
                Text(it, fontFamily = FontFamily.Monospace, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PyramidFigure(f: Figure.Pyramid) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        f.rows.forEach { row ->
            Row {
                row.forEach { cell ->
                    val isQuestion = cell == "?"
                    Box(
                        Modifier
                            .size(52.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.onSurface)
                            .background(if (isQuestion) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(cell, fontSize = 20.sp, fontWeight = if (isQuestion) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun VennFigure(f: Figure.Venn) {
    val measurer = rememberTextMeasurer()
    val ink = MaterialTheme.colorScheme.onSurface
    val leftFill = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val rightFill = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
    val valueStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ink)
    val labelStyle = TextStyle(fontSize = 15.sp, color = ink)
    Canvas(Modifier.widthIn(max = 420.dp).fillMaxWidth().height(200.dp)) {
        val r = min(size.height * 0.36f, size.width * 0.22f)
        val cy = size.height * 0.58f
        val cx = size.width / 2
        val left = Offset(cx - r * 0.62f, cy)
        val right = Offset(cx + r * 0.62f, cy)
        drawCircle(leftFill, r, left)
        drawCircle(rightFill, r, right)
        drawCircle(ink, r, left, style = Stroke(2.dp.toPx()))
        drawCircle(ink, r, right, style = Stroke(2.dp.toPx()))
        centeredText(measurer, f.leftLabel, Offset(left.x - r * 0.3f, cy - r - 14.dp.toPx()), labelStyle)
        centeredText(measurer, f.rightLabel, Offset(right.x + r * 0.3f, cy - r - 14.dp.toPx()), labelStyle)
        if (f.left.isNotEmpty()) centeredText(measurer, f.left, Offset(left.x - r * 0.5f, cy), valueStyle)
        if (f.both.isNotEmpty()) centeredText(measurer, f.both, Offset(cx, cy), valueStyle)
        if (f.right.isNotEmpty()) centeredText(measurer, f.right, Offset(right.x + r * 0.5f, cy), valueStyle)
    }
}

@Composable
private fun GridFigure(f: Figure.Grid) {
    val ink = MaterialTheme.colorScheme.onSurface
    val shade = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
    val cell = 48.dp
    Canvas(Modifier.size(cell * f.cols, cell * f.rows)) {
        val s = cell.toPx()
        f.shaded.forEach { i ->
            drawRect(shade, topLeft = Offset((i % f.cols) * s, (i / f.cols) * s), size = androidx.compose.ui.geometry.Size(s, s))
        }
        val w = 2.dp.toPx()
        for (r in 0..f.rows) drawLine(ink, Offset(0f, r * s), Offset(f.cols * s, r * s), w, StrokeCap.Square)
        for (c in 0..f.cols) drawLine(ink, Offset(c * s, 0f), Offset(c * s, f.rows * s), w, StrokeCap.Square)
    }
}

@Composable
private fun ClockFigure(f: Figure.Clock) {
    val measurer = rememberTextMeasurer()
    val ink = MaterialTheme.colorScheme.onSurface
    val face = MaterialTheme.colorScheme.surfaceVariant
    val minuteColor = MaterialTheme.colorScheme.primary
    val numberStyle = TextStyle(fontSize = 15.sp, color = ink)
    Canvas(Modifier.size(190.dp)) {
        val r = size.minDimension / 2 - 4.dp.toPx()
        val c = center
        fun at(fractionOfTurn: Double, radius: Float) = Offset(
            c.x + radius * sin(2 * PI * fractionOfTurn).toFloat(),
            c.y - radius * cos(2 * PI * fractionOfTurn).toFloat(),
        )
        drawCircle(face, r, c)
        drawCircle(ink, r, c, style = Stroke(3.dp.toPx()))
        for (i in 1..12) {
            drawLine(ink, at(i / 12.0, r * 0.92f), at(i / 12.0, r), 2.dp.toPx())
            centeredText(measurer, "$i", at(i / 12.0, r * 0.76f), numberStyle)
        }
        val hourTurn = ((f.hour % 12) + f.minute / 60.0) / 12.0
        drawLine(ink, c, at(hourTurn, r * 0.48f), 6.dp.toPx(), StrokeCap.Round)
        drawLine(minuteColor, c, at(f.minute / 60.0, r * 0.72f), 4.dp.toPx(), StrokeCap.Round)
        drawCircle(ink, 5.dp.toPx(), c)
    }
}

@Composable
private fun BalanceFigure(f: Figure.Balance) {
    val ink = MaterialTheme.colorScheme.onSurface
    Column(Modifier.widthIn(max = 420.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            listOf(f.left, f.right).forEach { side ->
                Column(
                    Modifier.weight(1f).padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    side.forEach { item ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(item, Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 15.sp)
                        }
                    }
                }
            }
        }
        Canvas(Modifier.fillMaxWidth().height(56.dp)) {
            val y = 6.dp.toPx()
            drawLine(ink, Offset(size.width * 0.06f, y), Offset(size.width * 0.94f, y), 4.dp.toPx(), StrokeCap.Round)
            val base = Path().apply {
                moveTo(size.width / 2, y)
                lineTo(size.width / 2 - 22.dp.toPx(), size.height - 2.dp.toPx())
                lineTo(size.width / 2 + 22.dp.toPx(), size.height - 2.dp.toPx())
                close()
            }
            drawPath(base, ink)
        }
    }
}

@Composable
private fun BarChartFigure(f: Figure.BarChart) {
    val measurer = rememberTextMeasurer()
    val ink = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val bar = MaterialTheme.colorScheme.primary
    val small = TextStyle(fontSize = 13.sp, color = ink)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth()) {
        Text(f.title, style = MaterialTheme.typography.titleSmall)
        Canvas(Modifier.fillMaxWidth().height(230.dp)) {
            val left = 34.dp.toPx()
            val bottom = size.height - 26.dp.toPx()
            val top = 10.dp.toPx()
            val maxValue = (ceil(f.values.max().toDouble() / f.step) * f.step).toInt().coerceAtLeast(f.step)
            fun yOf(v: Int) = bottom - (bottom - top) * v / maxValue
            var v = 0
            while (v <= maxValue) {
                drawLine(gridColor, Offset(left, yOf(v)), Offset(size.width, yOf(v)), 1.dp.toPx())
                centeredText(measurer, "$v", Offset(left / 2, yOf(v)), small)
                v += f.step
            }
            val slot = (size.width - left) / f.values.size
            f.values.forEachIndexed { i, value ->
                val x = left + slot * i + slot * 0.2f
                drawRect(bar, topLeft = Offset(x, yOf(value)), size = androidx.compose.ui.geometry.Size(slot * 0.6f, bottom - yOf(value)))
                centeredText(measurer, f.labels[i], Offset(x + slot * 0.3f, bottom + 13.dp.toPx()), small)
            }
            drawLine(ink, Offset(left, bottom), Offset(size.width, bottom), 2.dp.toPx())
            drawLine(ink, Offset(left, top), Offset(left, bottom), 2.dp.toPx())
        }
    }
}

@Composable
private fun TableFigure(f: Figure.Table) {
    val line = MaterialTheme.colorScheme.onSurface
    Column(Modifier.widthIn(max = 320.dp).fillMaxWidth().border(1.5.dp, line)) {
        (listOf(f.header) + f.rows).forEachIndexed { r, row ->
            Row(Modifier.background(if (r == 0) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)) {
                row.forEach { cell ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(42.dp)
                            .border(0.75.dp, line)
                            .background(if (cell == "?") MaterialTheme.colorScheme.secondaryContainer else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(cell, fontSize = 18.sp, fontWeight = if (r == 0 || cell == "?") FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapeFigure(f: Figure.Shape) {
    val measurer = rememberTextMeasurer()
    val ink = MaterialTheme.colorScheme.onSurface
    val fill = MaterialTheme.colorScheme.primaryContainer
    val gridColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val labelStyle = TextStyle(fontSize = 14.sp, color = ink, fontWeight = FontWeight.Medium)
    val pts = f.points
    val minX = pts.minOf { it.x }
    val minY = pts.minOf { it.y }
    val w = pts.maxOf { it.x } - minX
    val h = pts.maxOf { it.y } - minY
    val ratio = ((w + 3) / (h + 3)).toFloat().coerceIn(0.8f, 2.2f)
    // No fillMaxWidth here: aspectRatio can then shrink the width when the height is capped on TVs.
    Canvas(Modifier.widthIn(max = 420.dp).aspectRatio(ratio)) {
        val margin = 34.dp.toPx()
        val scale = min((size.width - 2 * margin) / w.toFloat(), (size.height - 2 * margin) / h.toFloat())
        val ox = (size.width - w.toFloat() * scale) / 2
        val oy = (size.height - h.toFloat() * scale) / 2
        fun map(x: Double, y: Double) = Offset(ox + ((x - minX) * scale).toFloat(), oy + ((y - minY) * scale).toFloat())
        val path = Path().apply {
            val start = map(pts[0].x, pts[0].y)
            moveTo(start.x, start.y)
            pts.drop(1).forEach { val o = map(it.x, it.y); lineTo(o.x, o.y) }
            close()
        }
        drawPath(path, fill)
        if (f.showGrid) {
            clipPath(path) {
                var x = ceil(minX)
                while (x <= minX + w) { drawLine(gridColor, map(x, minY), map(x, minY + h), 1.5.dp.toPx()); x += 1.0 }
                var y = ceil(minY)
                while (y <= minY + h) { drawLine(gridColor, map(minX, y), map(minX + w, y), 1.5.dp.toPx()); y += 1.0 }
            }
        }
        drawPath(path, ink, style = Stroke(3.dp.toPx()))
        f.sideLabels.forEach { (i, text) ->
            val a = pts[i]
            val b = pts[(i + 1) % pts.size]
            val mx = (a.x + b.x) / 2
            val my = (a.y + b.y) / 2
            val len = hypot(b.x - a.x, b.y - a.y)
            var nx = (b.y - a.y) / len
            var ny = -(b.x - a.x) / len
            if (insidePolygon(pts.map { it.x to it.y }, mx + nx * 0.05, my + ny * 0.05)) { nx = -nx; ny = -ny }
            val anchor = map(mx, my)
            val push = 18.dp.toPx()
            centeredText(measurer, text, Offset(anchor.x + (nx * push).toFloat(), anchor.y + (ny * push).toFloat()), labelStyle)
        }
    }
}

private fun insidePolygon(poly: List<Pair<Double, Double>>, px: Double, py: Double): Boolean {
    var inside = false
    var j = poly.size - 1
    for (i in poly.indices) {
        val (xi, yi) = poly[i]
        val (xj, yj) = poly[j]
        if ((yi > py) != (yj > py) && px < (xj - xi) * (py - yi) / (yj - yi) + xi) inside = !inside
        j = i
    }
    return inside
}
