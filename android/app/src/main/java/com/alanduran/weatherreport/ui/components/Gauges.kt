package com.alanduran.weatherreport.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alanduran.weatherreport.ui.theme.BadRed
import com.alanduran.weatherreport.ui.theme.CyanAccent
import com.alanduran.weatherreport.ui.theme.GoodGreen
import com.alanduran.weatherreport.ui.theme.MagentaAccent
import com.alanduran.weatherreport.ui.theme.TextPrimary
import com.alanduran.weatherreport.ui.theme.TextSecondary
import com.alanduran.weatherreport.ui.theme.TextTertiary
import com.alanduran.weatherreport.ui.theme.VioletAccent
import com.alanduran.weatherreport.ui.theme.VioletBright
import com.alanduran.weatherreport.ui.theme.WarnAmber
import java.time.Duration
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun UvArcGauge(index: Double, category: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.BottomCenter) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            val side = min(size.width, size.height * 2f)
            val topLeft = Offset((size.width - side) / 2f, stroke / 2f)
            drawArc(
                color = Color.White.copy(alpha = 0.12f),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(side, side),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            val fraction = (index / 11.0).coerceIn(0.0, 1.0).toFloat()
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(GoodGreen, WarnAmber, BadRed, MagentaAccent)
                ),
                startAngle = 180f,
                sweepAngle = 180f * fraction,
                useCenter = false,
                topLeft = topLeft,
                size = Size(side, side),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(
                text = index.roundToInt().toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}

@Composable
fun HumidityRing(value: Int, modifier: Modifier = Modifier) {
    val fraction = (value / 100f).coerceIn(0f, 1f)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = Color.White.copy(alpha = 0.12f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(CyanAccent, VioletAccent, CyanAccent)),
                startAngle = -90f,
                sweepAngle = 360f * fraction,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$value%",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
            Text(
                text = "Humedad",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}

@Composable
fun WindCompass(direction: Int, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = min(cx, cy) - 3.dp.toPx()
        val center = Offset(cx, cy)

        drawCircle(Color.White.copy(alpha = 0.07f), radius, center)
        drawCircle(Color.White.copy(alpha = 0.22f), radius, center, style = Stroke(1.dp.toPx()))

        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30).toDouble())
            val outer = Offset(
                cx + radius * cos(angle).toFloat(),
                cy + radius * sin(angle).toFloat()
            )
            val innerRadius = radius - if (i % 3 == 0) 8.dp.toPx() else 5.dp.toPx()
            val inner = Offset(
                cx + innerRadius * cos(angle).toFloat(),
                cy + innerRadius * sin(angle).toFloat()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.28f),
                start = inner,
                end = outer,
                strokeWidth = 1.dp.toPx()
            )
        }

        drawCanvasText("N", cx, cy - radius + 12.dp.toPx(), 9.sp.toPx(), TextSecondary, bold = true)
        drawCanvasText("E", cx + radius - 12.dp.toPx(), cy + 3.dp.toPx(), 9.sp.toPx(), TextSecondary, bold = true)
        drawCanvasText("S", cx, cy + radius - 4.dp.toPx(), 9.sp.toPx(), TextSecondary, bold = true)
        drawCanvasText("O", cx - radius + 12.dp.toPx(), cy + 3.dp.toPx(), 9.sp.toPx(), TextSecondary, bold = true)

        val radians = Math.toRadians((direction - 90).toDouble())
        val cosR = cos(radians).toFloat()
        val sinR = sin(radians).toFloat()
        val tipRadius = radius - 10.dp.toPx()
        val tip = Offset(cx + tipRadius * cosR, cy + tipRadius * sinR)
        val tail = Offset(cx - 9.dp.toPx() * cosR, cy - 9.dp.toPx() * sinR)
        drawLine(VioletBright, tail, tip, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        drawCircle(MagentaAccent, 4.dp.toPx(), tip)
        drawCircle(Color.White, 3.dp.toPx(), center)
    }
}

@Composable
fun SunArc(sunrise: LocalTime?, sunset: LocalTime?, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = 3.dp.toPx()
        val pad = 10.dp.toPx()
        val side = min(size.width - pad * 2, (size.height - pad) * 2f)
        val left = (size.width - side) / 2f
        val baseY = size.height - pad

        drawArc(
            color = Color.White.copy(alpha = 0.20f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(left, baseY - side / 2f),
            size = Size(side, side),
            style = Stroke(stroke, cap = StrokeCap.Round)
        )
        drawLine(
            color = Color.White.copy(alpha = 0.18f),
            start = Offset(left, baseY),
            end = Offset(left + side, baseY),
            strokeWidth = stroke
        )

        val progress = sunProgress(sunrise, sunset)
        val theta = Math.toRadians((180.0 - progress * 180.0))
        val radius = side / 2f
        val cx = left + radius
        val sunX = cx + radius * cos(theta).toFloat()
        val sunY = baseY - radius * sin(theta).toFloat()

        drawCircle(WarnAmber.copy(alpha = 0.30f), 13.dp.toPx(), Offset(sunX, sunY))
        drawCircle(WarnAmber, 5.dp.toPx(), Offset(sunX, sunY))
    }
}

@Composable
fun PressureGauge(pressure: Double, modifier: Modifier = Modifier) {
    val fraction = ((pressure - 980.0) / (1050.0 - 980.0)).coerceIn(0.0, 1.0).toFloat()
    Canvas(modifier) {
        val pad = 6.dp.toPx()
        val width = size.width - pad * 2
        val barHeight = 8.dp.toPx()
        val top = size.height / 2f - barHeight / 2f
        val radius = CornerRadius(barHeight / 2f)

        drawRoundRect(
            color = Color.White.copy(alpha = 0.12f),
            topLeft = Offset(pad, top),
            size = Size(width, barHeight),
            cornerRadius = radius
        )
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(CyanAccent, VioletAccent, MagentaAccent)),
            topLeft = Offset(pad, top),
            size = Size(width * fraction, barHeight),
            cornerRadius = radius
        )
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = Offset(pad + width * fraction, size.height / 2f)
        )
    }
}

private fun sunProgress(sunrise: LocalTime?, sunset: LocalTime?): Float {
    if (sunrise == null || sunset == null) return 0.5f
    val total = Duration.between(sunrise, sunset).toMinutes().toDouble()
    if (total <= 0.0) return 0.5f
    val elapsed = Duration.between(sunrise, LocalTime.now()).toMinutes().toDouble()
    return (elapsed / total).coerceIn(0.0, 1.0).toFloat()
}
