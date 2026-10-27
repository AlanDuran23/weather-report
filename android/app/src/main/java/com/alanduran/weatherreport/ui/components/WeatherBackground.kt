package com.alanduran.weatherreport.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.alanduran.weatherreport.ui.theme.NightIndigo
import com.alanduran.weatherreport.ui.theme.NightMagenta
import com.alanduran.weatherreport.ui.theme.NightPlum
import com.alanduran.weatherreport.ui.theme.NightViolet
import kotlin.random.Random

/**
 * Fondo dinamico de cielo nocturno nublado en tonos violetas.
 * Degradado + estrellas + nubes suaves que se desplazan lentamente.
 */
@Composable
fun WeatherBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "sky")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 45000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NightIndigo, NightViolet, NightPlum, NightMagenta)
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawStars()
            drawCloudLayer(drift)
        }
        content()
    }
}

private fun DrawScope.drawStars() {
    val random = Random(17)
    val count = 70
    repeat(count) {
        val x = random.nextFloat() * size.width
        val y = random.nextFloat() * size.height * 0.55f
        val radius = random.nextFloat() * 1.6f + 0.4f
        val alpha = 0.20f + random.nextFloat() * 0.55f
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = radius,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawCloudLayer(drift: Float) {
    val w = size.width
    val h = size.height

    val clusters = listOf(
        CloudCluster(0.10f, 0.16f, 0.55f, 0.30f),
        CloudCluster(0.72f, 0.10f, 0.62f, 0.24f),
        CloudCluster(0.38f, 0.30f, 0.72f, 0.20f),
        CloudCluster(-0.05f, 0.42f, 0.80f, 0.16f),
        CloudCluster(0.85f, 0.38f, 0.70f, 0.14f),
        CloudCluster(0.25f, 0.62f, 0.90f, 0.10f)
    )

    clusters.forEachIndexed { index, cluster ->
        val direction = if (index % 2 == 0) 1f else -1f
        val cx = (cluster.x + direction * drift * 0.06f) * w
        val cy = cluster.y * h
        val radius = cluster.radius * w
        softCloud(cx, cy, radius, cluster.alpha)
    }
}

private fun DrawScope.softCloud(cx: Float, cy: Float, radius: Float, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF9A7BD0).copy(alpha = alpha),
                Color(0xFF6C4BA8).copy(alpha = alpha * 0.35f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = radius
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
}

private data class CloudCluster(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float
)
