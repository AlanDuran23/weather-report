package com.alanduran.weatherreport.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb

internal fun DrawScope.drawCanvasText(
    text: String,
    x: Float,
    y: Float,
    sizePx: Float,
    color: Color,
    align: Paint.Align = Paint.Align.CENTER,
    bold: Boolean = false
) {
    val paint = Paint().apply {
        isAntiAlias = true
        this.color = color.toArgb()
        textSize = sizePx
        textAlign = align
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}
