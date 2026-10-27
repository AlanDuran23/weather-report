package com.alanduran.weatherreport.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alanduran.weatherreport.data.model.HourlyPoint
import com.alanduran.weatherreport.ui.theme.CyanAccent
import com.alanduran.weatherreport.ui.theme.MagentaAccent
import com.alanduran.weatherreport.ui.theme.TextPrimary
import com.alanduran.weatherreport.ui.theme.TextTertiary
import com.alanduran.weatherreport.ui.theme.VioletAccent
import com.alanduran.weatherreport.ui.theme.VioletBright
import com.alanduran.weatherreport.util.formatHour
import com.alanduran.weatherreport.util.formatTemperature
import com.alanduran.weatherreport.util.weatherConditionFromCode
import kotlin.math.roundToInt

@Composable
fun TemperatureLineChart(points: List<HourlyPoint>, modifier: Modifier = Modifier) {
    if (points.size < 2) {
        Box(modifier)
        return
    }
    val temperatures = points.map { it.temperature }
    val minTemp = temperatures.minOrNull() ?: 0.0
    val maxTemp = temperatures.maxOrNull() ?: 1.0
    val span = (maxTemp - minTemp).takeIf { it > 0.5 } ?: 1.0

    Canvas(modifier) {
        val padTop = 14.dp.toPx()
        val padBottom = 24.dp.toPx()
        val padX = 10.dp.toPx()
        val chartWidth = size.width - padX * 2
        val chartHeight = size.height - padTop - padBottom
        val stepX = chartWidth / (points.size - 1)

        val offsets = points.mapIndexed { index, point ->
            val x = padX + stepX * index
            val y = padTop + chartHeight * (1f - ((point.temperature - minTemp) / span).toFloat())
            Offset(x, y)
        }

        val fillPath = Path().apply {
            moveTo(offsets.first().x, padTop + chartHeight)
            offsets.forEach { lineTo(it.x, it.y) }
            lineTo(offsets.last().x, padTop + chartHeight)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(VioletAccent.copy(alpha = 0.45f), Color.Transparent),
                startY = padTop,
                endY = padTop + chartHeight
            )
        )

        val linePath = Path().apply {
            moveTo(offsets.first().x, offsets.first().y)
            for (index in 1 until offsets.size) {
                val previous = offsets[index - 1]
                val current = offsets[index]
                val midX = (previous.x + current.x) / 2f
                cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
            }
        }
        drawPath(
            path = linePath,
            brush = Brush.horizontalGradient(listOf(VioletBright, MagentaAccent)),
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        offsets.forEach { drawCircle(Color.White.copy(alpha = 0.9f), 2.5.dp.toPx(), it) }

        points.forEachIndexed { index, point ->
            if (index % 6 == 0 || index == points.lastIndex) {
                drawCanvasText(
                    text = formatHour(point.time),
                    x = offsets[index].x,
                    y = size.height - 6.dp.toPx(),
                    sizePx = 10.sp.toPx(),
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun TempRangeBar(
    min: Double,
    max: Double,
    globalMin: Double,
    globalMax: Double,
    modifier: Modifier = Modifier
) {
    val span = (globalMax - globalMin).takeIf { it > 0.5 } ?: 1.0
    Canvas(modifier) {
        val barHeight = 6.dp.toPx()
        val top = (size.height - barHeight) / 2f
        val radius = CornerRadius(barHeight / 2f)
        val startFraction = ((min - globalMin) / span).coerceIn(0.0, 1.0).toFloat()
        val endFraction = ((max - globalMin) / span).coerceIn(0.0, 1.0).toFloat()
        val x0 = size.width * startFraction
        val x1 = size.width * endFraction

        drawRoundRect(
            color = Color.White.copy(alpha = 0.12f),
            topLeft = Offset(0f, top),
            size = Size(size.width, barHeight),
            cornerRadius = radius
        )
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(CyanAccent, VioletBright, MagentaAccent)),
            topLeft = Offset(x0, top),
            size = Size((x1 - x0).coerceAtLeast(barHeight), barHeight),
            cornerRadius = radius
        )
    }
}

@Composable
fun HourlyStrip(points: List<HourlyPoint>, modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(points) { point ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatHour(point.time),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Text(
                    text = weatherConditionFromCode(point.weatherCode).emoji,
                    fontSize = 20.sp
                )
                Text(
                    text = formatTemperature(point.temperature),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${point.windSpeed.roundToInt()} km/h",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
        }
    }
}
