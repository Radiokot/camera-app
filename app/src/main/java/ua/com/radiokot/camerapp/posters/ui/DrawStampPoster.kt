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

package ua.com.radiokot.camerapp.posters.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.util.fastRoundToInt
import ua.com.radiokot.camerapp.ui.AppColors
import ua.com.radiokot.camerapp.ui.drawPaperBackground

fun DrawScope.drawStampPoster(
    layers: Collection<UiStampPosterLayer>,
    colors: AppColors,
    textMeasurer: TextMeasurer,
) {
    drawPaperBackground(
        lineColor = colors.paperBackgroundLine,
        backgroundColor = colors.componentBackground,
        gridSizePx = (57 * density).fastRoundToInt(),
        gridThicknessPx = 3.6f * density,
        verticalOffsetPx = 0,
    )

    for (layer in layers) {
        val center = layer.center.value * density
        val rotationDegrees = layer.rotationDegrees.floatValue

        drawContext.canvas.rotate(
            degrees = rotationDegrees,
            pivotX = center.x,
            pivotY = center.y,
        )

        when (layer) {
            is UiStampPosterLayer.Stamp -> {
                val rect = layer.rect
                drawRect(
                    color = Color.Magenta,
                    topLeft = rect.topLeft * density,
                    size = rect.size * density,
                )
            }

            is UiStampPosterLayer.Text -> {
                layer.textMeasurer = textMeasurer
                val (rect, textLayout) = layer.rectAndLayout
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = rect.topLeft * density,
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
