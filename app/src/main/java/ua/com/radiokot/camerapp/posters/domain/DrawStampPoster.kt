/* Copyright 2026 Oleg Koretsky

   This file is part of the Press-Cut,
   a digital postage stamp cutter Android app.

   Press-Cut is free software: you can redistribute it
   and/or modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation, either version 3 of the License,
   or (at your option) any later version.

   Press-Cut is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   See the GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Press-Cut. If not, see <http://www.gnu.org/licenses/>.
*/

package ua.com.radiokot.camerapp.posters.domain

import android.graphics.CornerPathEffect
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.graphics.scale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.roundToIntSize
import androidx.compose.ui.util.fastRoundToInt
import ua.com.radiokot.camerapp.ui.DarkAppColors
import ua.com.radiokot.camerapp.ui.LightAppColors
import ua.com.radiokot.camerapp.ui.drawPaperBackground
import ua.com.radiokot.camerapp.ui.drawRectangleShadow

fun DrawScope.drawStampPoster(
    layers: Collection<StampPosterLayer>,
    isDark: Boolean,
) {
    val colors = if (isDark) DarkAppColors else LightAppColors

    drawPaperBackground(
        lineColor = colors.paperBackgroundLine,
        backgroundColor = colors.componentBackground,
        gridSizePx = (57 * density).fastRoundToInt(),
        gridThicknessPx = 3.6f * density,
        verticalOffsetPx = 0,
    )

    for (layer in layers) {
        val center = layer.center * density
        val rotationDegrees = layer.rotationDegrees

        drawContext.canvas.rotate(
            degrees = rotationDegrees,
            pivotX = center.x,
            pivotY = center.y,
        )

        when (layer) {
            is StampPosterLayer.Stamp -> {
                val size = layer.rect.size * density
                val topLeft = layer.rect.topLeft * density
                val imageBitmap = layer.imageBitmap

                drawRectangleShadow(
                    color = colors.stampShadow,
                    topLeft = topLeft,
                    size = size,
                    radiusDp = 48f,
                )

                if (imageBitmap != null) {
                    drawImage(
                        image = imageBitmap,
                        dstOffset = topLeft.round(),
                        dstSize = size.roundToIntSize(),
                    )
                } else {
                    drawRect(
                        color = Color.Yellow,
                        topLeft = topLeft,
                        size = size,
                    )
                }
            }

            is StampPosterLayer.Text -> {
                drawContext.canvas.scale(
                    sx = density,
                    pivotX = 0f,
                    pivotY = 0f,
                )

                val background = layer.appearance.background
                val textColors = background?.colors ?: colors

                if (background != null) {
                    drawStampPosterTextBackground(
                        path = layer.backgroundPath,
                        color = textColors.componentBackground,
                        scale = layer.scale,
                    )
                }

                drawText(
                    textLayoutResult = layer.textLayout,
                    topLeft = layer.rect.topLeft,
                    color = textColors.textPrimary,
                )

                drawContext.canvas.scale(
                    sx = 1f / density,
                    pivotX = 0f,
                    pivotY = 0f,
                )
            }
        }

        drawContext.canvas.rotate(
            degrees = -rotationDegrees,
            pivotX = center.x,
            pivotY = center.y,
        )
    }
}

fun DrawScope.drawStampPosterTextBackground(
    path: Path,
    color: Color,
    scale: Float,
) {
    stampPosterTextBackgroundPaint.pathEffect =
        CornerPathEffect(20f * scale)
    stampPosterTextBackgroundPaint.color =
        color.toArgb()

    drawContext.canvas.nativeCanvas.drawPath(
        path.asAndroidPath(),
        stampPosterTextBackgroundPaint,
    )
}

private val stampPosterTextBackgroundPaint = Paint().apply {
    style = Paint.Style.FILL
}
