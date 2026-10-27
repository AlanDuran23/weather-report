package com.alanduran.weatherreport.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Efecto "frosted glass": superficie semitransparente con borde luminoso.
 * En API < 31 no hay desenfoque real de fondo, por lo que se simula con
 * transparencia + degradado sutil.
 */
fun Modifier.glassCard(corner: Dp = 22.dp, tint: Float = 0.10f): Modifier {
    val shape = RoundedCornerShape(corner)
    return this
        .clip(shape)
        .background(
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = tint + 0.05f),
                    Color.White.copy(alpha = tint * 0.45f)
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.06f)
                )
            ),
            shape = shape
        )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 22.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .glassCard(corner)
            .padding(contentPadding),
        content = content
    )
}
