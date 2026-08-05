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

import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.roundToIntSize
import androidx.compose.ui.util.fastRoundToInt
import ua.com.radiokot.camerapp.ui.DarkAppColors
import ua.com.radiokot.camerapp.ui.LightAppColors
import ua.com.radiokot.camerapp.ui.drawPaperBackground

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

    val shadowPaint = stampPosterShadowPaint.apply {
        setShadowLayer(
            48f * density,
            0f,
            0f,
            colors.stampShadow.toArgb()
        )
    }

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
                val rect = layer.rect
                val imageBitmap = layer.imageBitmap

                drawContext.canvas.nativeCanvas.drawRect(
                    rect.left * density,
                    rect.top * density,
                    rect.right * density,
                    rect.bottom * density,
                    shadowPaint,
                )

                if (imageBitmap != null) {
                    drawImage(
                        image = imageBitmap,
                        dstOffset = (rect.topLeft * density).round(),
                        dstSize = (rect.size * density).roundToIntSize(),
                    )
                } else {
                    drawRect(
                        color = Color.Yellow,
                        topLeft = rect.topLeft * density,
                        size = rect.size * density,
                    )
                }
            }

            is StampPosterLayer.Text -> {
                drawText(
                    textLayoutResult =
                        layer.getTextLayoutToDraw(
                            drawDensity = density,
                        ),
                    topLeft = layer.rect.topLeft * density,
                    color = colors.textPrimary,
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

private val stampPosterShadowPaint = Paint().apply {
    style = Paint.Style.FILL
    color = android.graphics.Color.TRANSPARENT
}
